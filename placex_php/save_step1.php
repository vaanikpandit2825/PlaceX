<?php
// save_step1.php
require 'db.php';
$id      = intval($_POST['student_id'] ?? 0);
$name    = trim($_POST['name']    ?? '');
$phone   = trim($_POST['phone']   ?? '');
$dob     = trim($_POST['dob']     ?? '');
$gender  = trim($_POST['gender']  ?? '');
$address = trim($_POST['address'] ?? '');

$stmt = $conn->prepare("UPDATE Student SET name=?,phone=?,dob=?,gender=?,address=? WHERE student_id=?");
$stmt->bind_param("sssssi", $name, $phone, $dob, $gender, $address, $id);
echo $stmt->execute() ? "Success" : "Update failed: " . $conn->error;
$conn->close();
?>
