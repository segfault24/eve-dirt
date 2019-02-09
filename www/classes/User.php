<?php
namespace Dirt;

use PDO;

/*
 * $_SESSION[]
 * userid
 * username
 * admin set if the user is an administrator
 * last_active
 *
 * charid set if the user has a linked character
 * charname
 * auth_token
 * token_expires
 * refresh_token
 */
class User
{

    private static $instance = NULL;

    private function __construct()
    {
        // if there isn't an active session for the client provided session id,
        // regenerate the session
        if (! isset($_SESSION['canary'])) {
            session_regenerate_id(true);
            $_SESSION['canary'] = [
                'ip' => hash('sha256', $_SERVER['REMOTE_ADDR']),
                'usragnt' => hash('sha256', $_SERVER['HTTP_USER_AGENT'])
            ];
        }

        // check for session timeout
        if (isset($_SESSION['last_active']) && (time() - $_SESSION['last_active'] > 86400)) {
            session_unset();
            session_destroy();
        }

        // prevent session hijacking
        if ($_SESSION['canary']['ip'] != hash('sha256', $_SERVER['REMOTE_ADDR']) || $_SESSION['canary']['usragnt'] != hash('sha256', $_SERVER['HTTP_USER_AGENT'])) {
            session_regenerate_id(true);
            $_SESSION['canary'] = [
                'ip' => hash('sha256', $_SERVER['REMOTE_ADDR']),
                'usragnt' => hash('sha256', $_SERVER['HTTP_USER_AGENT'])
            ];
        }

        $_SESSION['last_active'] = time();
    }

    public static function getUser()
    {
        if (User::$instance == NULL) {
            User::$instance = new User();
        }
        return User::$instance;
    }

    public function login($user, $pass)
    {
        $db = Database::getDb();

        // get the user's info from the db
        $sql = 'SELECT `userId`, `name`, `hash`, `admin`, `disabled` FROM dirtUser WHERE `username`=:username';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':username', $user);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        $retval = false;
        if ($row) {
            // found user by that username
            // verify the given password and that the account isn't disabled
            if (password_verify($pass, $row['hash']) && $row['disabled'] == 0) {
                // successfully verified password
                // ensure login is allowed if maintenance mode is active
                $mm = filter_var(Tools::getProperty('maintenancemode'), FILTER_VALIDATE_BOOLEAN);
                if (! $mm || ($mm && $row['admin'] == 1)) {

                    // get the user's information
                    $_SESSION['userid'] = $row['userId'];
                    $_SESSION['username'] = $row['name'];

                    if ($row['admin'] == 1) {
                        $_SESSION['admin'] = 1;
                    }

                    // update the user's last login
                    $sql = 'UPDATE dirtUser SET `lastLogin`=NOW() WHERE `userId`=:userid';
                    $stmt = $db->prepare($sql);
                    $stmt->bindParam(':userid', $row['userId']);
                    $stmt->execute();

                    // get the user's character information
                    $this->setActiveCharAny();

                    $retval = true;
                }
            } else {
                // bad password
            }
        } else {
            // bad username
        }

        return $retval;
    }

    public function logout()
    {
        session_unset();
        session_destroy();
    }

    public function linkCharacter($charid, $charhash, $charname, $token, $expires, $refresh)
    {
        // store the tokens, charid, charname, charhash in db
        $db = Database::getDb();
        $userid = $this->getUserId();

        // check if this char is already linked
        $sql = 'SELECT `charId` FROM dirtApiAuth WHERE `userId`=:userid AND `charId`=:charid';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':userid', $userid);
        $stmt->bindParam(':charid', $charid);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($row) {
            return true; // already linked, pretend we were successful
        }

        $expires_timestamp = date('Y-m-d H:i:s', strtotime('now +' . $expires . ' seconds'));

        $sql = 'INSERT INTO dirtApiAuth (`userId`, `charId`, `charName`, `charHash`, `token`, `expires`, `refresh`)
				VALUES (:userid, :charid, :charname, :charhash, :token, :expires, :refresh);';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':userid', $userid);
        $stmt->bindParam(':charid', $charid);
        $stmt->bindParam(':charname', $charname);
        $stmt->bindParam(':charhash', $charhash);
        $stmt->bindParam(':token', $token);
        $stmt->bindParam(':expires', $expires_timestamp);
        $stmt->bindParam(':refresh', $refresh);
        $ret = $stmt->execute();

        if ($ret) {
            // sql query successful
            if (! $this->hasActiveChar()) {
                // make this the active char if none set yet
                // add the info to the session vars
                $this->setActiveChar($charid);
            }
            return true;
        } else {
            // query failed, don't link
            return false;
        }
    }

    public function unlinkCharacter($charid)
    {
        // can't unlink what's not there
        if (! $this->hasActiveChar()) {
            return false;
        }

        // delete the user's character from the db
        // we match the userid here as well as the charid
        // to prevent mitigate on the form hidden input
        $db = Database::getDb();
        $sql = 'DELETE FROM dirtApiAuth WHERE userId=:userid AND charId=:charid;';
        $stmt = $db->prepare($sql);
        $userid = $this->getUserId();
        $stmt->bindParam(':userid', $userid);
        $stmt->bindParam(':charid', $charid);
        $ret = $stmt->execute();

        if ($ret) {
            if ($charid == $this->getActiveCharId()) {
                // clear the active char if it was the one we just unlinked
                $this->clearActiveChar();
                // activate another linked char (if there is one)
                $this->setActiveCharAny();
            }
            return true;
        } else {
            return false;
        }
    }

    public function isLoggedIn()
    {
        return isset($_SESSION['userid']);
    }

    public function isAdmin()
    {
        return $this->isLoggedIn() && isset($_SESSION['admin']);
    }

    public function getUserId()
    {
        if ($this->isLoggedIn()) {
            return $_SESSION['userid'];
        } else {
            return - 1;
        }
    }

    public function getUserName()
    {
        return $_SESSION['username'];
    }

    public function hasActiveChar()
    {
        return $this->isLoggedIn() && isset($_SESSION['charid']);
    }

    public function getActiveCharId()
    {
        if ($this->hasActiveChar()) {
            return $_SESSION['charid'];
        } else {
            return - 1;
        }
    }

    public function getActiveCharName()
    {
        if ($this->hasActiveChar()) {
            return $_SESSION['charname'];
        } else {
            return '';
        }
    }

    public function getAuthToken()
    {
        if (! $this->hasActiveChar()) {
            return null;
        }

        $exp = strtotime($_SESSION['token_expires']);
        $now = time();
        if ($now > $exp) {
            $this->doTokenRefresh();
        }

        return $_SESSION['auth_token'];
    }

    public function getRefreshToken()
    {
        return $_SESSION['refresh_token'];
    }

    public function setTemplateVars(&$args)
    {
        $args['name'] = $this->getUserName();

        if ($this->isAdmin()) {
            $args['admin'] = 1;
        }

        if ($this->hasActiveChar()) {
            $args['char'] = 1;
            $args['charid'] = $this->getActiveCharId();
            $args['name'] = $this->getActiveCharName();
        }
    }

    /**
     * Sets the user's active character as one of their linked chars.
     *
     * @return boolean
     */
    private function setActiveCharAny()
    {
        $db = Database::getDb();
        $sql = 'SELECT `charId`, `charName`, `token`, `expires`, `refresh` FROM dirtApiAuth WHERE `userId`=:userid LIMIT 1';
        $stmt = $db->prepare($sql);
        $userid = $this->getUserId();
        $stmt->bindParam(':userid', $userid);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($row) {
            $_SESSION['charid'] = $row['charId'];
            $_SESSION['charname'] = $row['charName'];
            $_SESSION['auth_token'] = $row['token'];
            $_SESSION['token_expires'] = $row['expires'];
            $_SESSION['refresh_token'] = $row['refresh'];
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the given character (by charid) as the user's active character.
     *
     * @param
     *            charid
     * @return boolean
     */
    public function setActiveChar($charid)
    {
        $db = Database::getDb();
        $sql = 'SELECT `charId`, `charName`, `token`, `expires`, `refresh` FROM dirtApiAuth WHERE `userId`=:userid AND `charId`=:charid';
        $stmt = $db->prepare($sql);
        $userid = $this->getUserId();
        $stmt->bindParam(':userid', $userid);
        $stmt->bindParam(':charid', $charid);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($row) {
            $_SESSION['charid'] = $row['charId'];
            $_SESSION['charname'] = $row['charName'];
            $_SESSION['auth_token'] = $row['token'];
            $_SESSION['token_expires'] = $row['expires'];
            $_SESSION['refresh_token'] = $row['refresh'];
            return true;
        } else {
            return false;
        }
    }

    private function clearActiveChar()
    {
        unset($_SESSION['charid']);
        unset($_SESSION['charname']);
        unset($_SESSION['auth_token']);
        unset($_SESSION['token_expires']);
        unset($_SESSION['refresh_token']);
    }

    private function doTokenRefresh()
    {
        // grab the old stuff
        $old_token = $_SESSION['auth_token'];
        $old_expires_timestamp = $_SESSION['token_expires'];
        $old_refresh_token = $_SESSION['refresh_token'];

        // do the refresh
        $result = Tools::oauthRefresh($old_refresh_token);
        ;
        if ($result == false) {
            return;
        }
        $rsp = json_decode($result);
        if (! isset($rsp->access_token)) {
            return;
        }

        // get the new values
        $new_token = $rsp->access_token;
        $new_expires = $rsp->expires_in;
        $new_expires_timestamp = date('Y-m-d H:i:s', strtotime('now +' . $new_expires . ' seconds'));
        $new_refresh_token = $rsp->refresh_token;
        $userid = $_SESSION['userid'];
        $charid = $_SESSION['charid'];

        // update the session
        $_SESSION['auth_token'] = $new_token;
        $_SESSION['token_expires'] = $new_expires_timestamp;
        $_SESSION['refresh_token'] = $new_refresh_token;

        // update the database
        $db = Database::getDb();
        $sql = 'UPDATE dirtApiAuth SET `token`=:token, `expires`=:expires, `refresh`=:refresh';
        $sql .= ' WHERE `userId`=:userid AND `charId`=:charid;';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':token', $new_token);
        $stmt->bindParam(':expires', $new_expires_timestamp);
        $stmt->bindParam(':refresh', $new_refresh_token);
        $stmt->bindParam(':userid', $userid);
        $stmt->bindParam(':charid', $charid);
        $ret = $stmt->execute();
    }
}
