<?php
// mark_notification_read.php
require 'db.php';
$student_id = intval($_POST['student_id'] ?? 0);
$conn->prepare("UPDATE Notification SET is_read=1 WHERE student_id=?")->bind_param("i",$student_id);
$stmt = $conn->prepare("UPDATE Notification SET is_read=1 WHERE student_id=?");
$stmt->bind_param("i", $student_id);
echo $stmt->execute() ? "Success" : "Failed";
$conn->close();
?>
