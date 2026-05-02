<?php
// get_notifications.php
require 'db.php';
$student_id = intval($_POST['student_id'] ?? 0);

$stmt = $conn->prepare("
    SELECT notification_id, message, is_read, created_at
    FROM   Notification
    WHERE  student_id = ?
    ORDER  BY created_at DESC
    LIMIT  30
");
$stmt->bind_param("i", $student_id);
$stmt->execute();
$res = $stmt->get_result();

$notifs = [];
$unread = 0;
while ($r = $res->fetch_assoc()) {
    $notifs[] = $r;
    if ($r['is_read'] == 0) $unread++;
}
echo json_encode(['notifications' => $notifs, 'unread_count' => $unread]);
$conn->close();
?>
