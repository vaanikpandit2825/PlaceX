<?php
// get_resume.php
require 'db.php';
$id   = intval($_POST['student_id'] ?? 0);
$stmt = $conn->prepare("SELECT * FROM Resume WHERE student_id=?");
$stmt->bind_param("i", $id);
$stmt->execute();
$row = $stmt->get_result()->fetch_assoc();
echo $row ? json_encode($row) : json_encode(['skills'=>'','experience'=>'','projects'=>'','certifications'=>'']);
$conn->close();
?>
