<?php
// admin_approve_company.php
require 'db.php';
$company_id = intval($_POST['company_id'] ?? 0);
$action     = trim($_POST['action']       ?? ''); // approve | reject

$company_id = intval($_REQUEST['company_id'] ?? 0);
$action     = trim($_REQUEST['action'] ?? '');

$status = $action === 'approve' ? 1 : 2;
$stmt   = $conn->prepare("UPDATE Company SET is_approved=? WHERE company_id=?");
$stmt->bind_param("ii", $status, $company_id);
echo $stmt->execute() ? "Success" : "Failed: " . $conn->error;
$conn->close();

?>
