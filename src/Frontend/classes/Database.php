<?php

namespace Dirt;

use \PDO;
use PDOException;

class Database {
	
	private static $instance = NULL;
	
	private $db;
	
	private function __construct() {
		try {
			$this->db = new PDO(
				Site::DB_DRVR.':host='.Site::DB_ADDR.';port='.SITE::DB_PORT.';dbname='.SITE::DB_NAME.';charset=utf8',
				Site::DB_USER,
				Site::DB_PASS);
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
