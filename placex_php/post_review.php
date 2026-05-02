<?php
// post_review.php
require 'db.php';
$student_id = intval($_POST['student_id'] ?? 0);
$company_id = intval($_POST['company_id'] ?? 0);
$rating     = intval($_POST['rating']     ?? 0);
$comment    = trim($_POST['comment']      ?? '');

if ($rating < 1 || $rating > 5) { echo "Rating must be 1-5"; exit; }

// Only allow students who applied to this company
$chk = $conn->prepare("
    SELECT 1 FROM Application A
    INNER JOIN Job J ON A.job_id = J.job_id
    WHERE A.student_id=? AND J.company_id=?
");
$chk->bind_param("ii", $student_id, $company_id);
$chk->execute();
if ($chk->get_result()->num_rows === 0) { echo "You can only review companies you applied to"; exit; }

$stmt = $conn->prepare("
    INSERT INTO Review(student_id, company_id, rating, comment)
    VALUES(?,?,?,?)
    ON DUPLICATE KEY UPDATE rating=VALUES(rating), comment=VALUES(comment)
");
$stmt->bind_param("iiis", $student_id, $company_id, $rating, $comment);
echo $stmt->execute() ? "Success" : "Failed: " . $conn->error;
$conn->close();
?>
