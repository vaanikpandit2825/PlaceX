<?php
// db.php — shared database connection
// Place ALL php files inside: htdocs/placex/
$host = 'localhost';
$db   = 'placex';
$user = 'root';
$pass = '';   // change if your MySQL has a password

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    http_response_code(500);
    die(json_encode(['error' => 'DB connection failed: ' . $conn->connect_error]));
}
$conn->set_charset('utf8mb4');
?>
