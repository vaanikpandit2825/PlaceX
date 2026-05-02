<?php
// recruiter_update_status.php
require 'db.php';
$app_id      = intval($_POST['application_id'] ?? 0);
$status      = trim($_POST['status']           ?? '');
$company_id  = intval($_POST['company_id']     ?? 0);

// Recruiters can only move to: Shortlisted, Interview Scheduled, Interview Done, Selected, Rejected
$allowed = ['Shortlisted','Interview Scheduled','Interview Done','Selected','Rejected'];
if (!in_array($status, $allowed)) { echo "Invalid status"; exit; }

// Verify application belongs to a job of this company
$chk = $conn->prepare("
    SELECT A.application_id FROM Application A
    INNER JOIN Job J ON A.job_id = J.job_id
    WHERE A.application_id=? AND J.company_id=?
");
$chk->bind_param("ii", $app_id, $company_id);
$chk->execute();
if ($chk->get_result()->num_rows === 0) { echo "Unauthorized"; exit; }

$stmt = $conn->prepare("UPDATE Application SET status=? WHERE application_id=?");
$stmt->bind_param("si", $status, $app_id);
echo $stmt->execute() ? "Success" : "Failed: " . $conn->error;
$conn->close();
?>
