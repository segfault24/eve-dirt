<?php

namespace Dirt;

use \PDO;
use PDOException;

class Database {
	
	private static $instance = NULL;
	
	private $db;
	
	private function __construct() {
	    
	    $db_ini = parse_ini_file(__DIR__ . "../../cfg/db.ini");
	    
		try {
			$this->db = new PDO(
			    $db_ini['dbdriver'].':host='.$db_ini['dbhost'].';port='.$db_ini['dbport'].';dbname='.$db_ini['dbname'].';charset=utf8',
			    $db_ini['dbuser'],
			    $db_ini['dbpass']);
			$this->db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING);
		} catch(PDOException $e) {
			$this->db = NULL;
		}
	}
	
	public static function getDb() {
		if(Database::$instance == NULL) {
			Database::$instance = new Database();
		}
		return Database::$instance->db;
	}
	
}
