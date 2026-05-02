<?php
// get_applications.php — 3-table JOIN with rejection count
require 'db.php';
$student_id = intval($_POST['student_id'] ?? 0);

$sql = "
    SELECT A.application_id, A.status, A.applied_at, A.updated_at,
           J.title, J.package_lpa, J.location, J.job_type, J.job_id,
           C.company_name, C.industry, C.company_id
    FROM   Application A
    INNER  JOIN Job     J ON A.job_id     = J.job_id
    INNER  JOIN Company C ON J.company_id = C.company_id
    WHERE  A.student_id = ?
    ORDER  BY A.applied_at DESC
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $student_id);
$stmt->execute();
$res  = $stmt->get_result();

$apps = [];
$rejections = 0;
while ($row = $res->fetch_assoc()) {
    $apps[] = $row;
    if ($row['status'] === 'Rejected') $rejections++;
}

// Skill suggestion system: if 3+ rejections flag it
echo json_encode([
    'applications'    => $apps,
    'rejection_count' => $rejections,
    'suggest_skills'  => $rejections >= 3
]);
$conn->close();
?>
