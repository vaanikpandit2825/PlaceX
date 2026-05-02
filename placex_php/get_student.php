<?php
// get_student.php
require 'db.php';
$id = intval($_POST['student_id'] ?? 0);
$stmt = $conn->prepare("SELECT * FROM Student WHERE student_id=?");
$stmt->bind_param("i", $id);
$stmt->execute();
$row = $stmt->get_result()->fetch_assoc();
if ($row) {
    echo json_encode($row);
} else {
    echo json_encode(['error' => 'Not found']);
}
$conn->close();
?>
