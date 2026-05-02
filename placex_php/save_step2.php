<?php
// save_step2.php
require 'db.php';
$id      = intval($_POST['student_id']    ?? 0);
$college = trim($_POST['college']         ?? '');
$tenth   = floatval($_POST['tenth']       ?? 0);
$twelfth = floatval($_POST['twelfth']     ?? 0);
$cgpa    = floatval($_POST['cgpa']        ?? 0);

$stmt = $conn->prepare("UPDATE Student SET college=?,tenth_percent=?,twelfth_percent=?,cgpa=?,profile_complete=1 WHERE student_id=?");
$stmt->bind_param("sdddi", $college, $tenth, $twelfth, $cgpa, $id);
echo $stmt->execute() ? "Success" : "Update failed: " . $conn->error;
$conn->close();
?>
