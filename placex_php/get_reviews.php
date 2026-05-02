<?php
// get_reviews.php
require 'db.php';
$company_id = intval($_POST['company_id'] ?? 0);

$stmt = $conn->prepare("
    SELECT R.rating, R.comment, R.created_at,
           S.name AS student_name, S.branch
    FROM   Review R
    INNER  JOIN Student S ON R.student_id = S.student_id
    WHERE  R.company_id = ?
    ORDER  BY R.created_at DESC
");
$stmt->bind_param("i", $company_id);
$stmt->execute();
$res = $stmt->get_result();

$reviews = [];
while ($r = $res->fetch_assoc()) $reviews[] = $r;

// Avg rating
$avg = $conn->prepare("SELECT ROUND(AVG(rating),1) FROM Review WHERE company_id=?");
$avg->bind_param("i", $company_id);
$avg->execute();
$avg_rating = $avg->get_result()->fetch_row()[0] ?? 0;

echo json_encode(['reviews' => $reviews, 'avg_rating' => $avg_rating]);
$conn->close();
?>
