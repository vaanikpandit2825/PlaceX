<?php
// get_application_history.php — full workflow timeline for one application
require 'db.php';
$app_id = intval($_POST['application_id'] ?? 0);

$stmt = $conn->prepare("
    SELECT H.status, H.changed_at
    FROM   ApplicationHistory H
    WHERE  H.application_id = ?
    ORDER  BY H.changed_at ASC
");
$stmt->bind_param("i", $app_id);
$stmt->execute();
$res = $stmt->get_result();

$history = [];
while ($row = $res->fetch_assoc()) {
    $history[] = $row;
}
echo json_encode($history);
$conn->close();
?>
