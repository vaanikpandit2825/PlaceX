<?php
// admin_update_status.php
// Triggers in MySQL handle: history logging + notification + is_placed update
require 'db.php';

$app_id = intval($_POST['application_id'] ?? 0);
$status = trim($_POST['status']           ?? '');

$allowed = ['Applied','Shortlisted','Interview Scheduled','Interview Done','Selected','Rejected','Offer Accepted'];
if (!in_array($status, $allowed)) { echo "Invalid status"; exit; }

$stmt = $conn->prepare("UPDATE Application SET status=? WHERE application_id=?");
$stmt->bind_param("si", $status, $app_id);
echo $stmt->execute() ? "Success" : "Failed: " . $conn->error;
$conn->close();
?>
