<?php
// admin_export.php — returns placement report as JSON (Android generates CSV from it)
require 'db.php';

$res = $conn->query("
    SELECT S.name, S.email, S.branch, S.college, S.cgpa,
           S.tenth_percent, S.twelfth_percent,
           CASE WHEN S.is_placed=1 THEN 'Yes' ELSE 'No' END AS placed,
           COUNT(A.application_id)                            AS total_applications,
           GROUP_CONCAT(DISTINCT C.company_name ORDER BY C.company_name SEPARATOR ', ') AS companies_applied
    FROM   Student S
    LEFT   JOIN Application A ON A.student_id = S.student_id
    LEFT   JOIN Job         J ON J.job_id     = A.job_id
    LEFT   JOIN Company     C ON C.company_id = J.company_id
    GROUP  BY S.student_id
    ORDER  BY S.cgpa DESC
");

$rows = [];
while ($r = $res->fetch_assoc()) $rows[] = $r;
echo json_encode($rows);
$conn->close();
?>
