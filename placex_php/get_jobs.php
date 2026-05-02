<?php
// get_jobs.php
// Returns jobs that the student is eligible for (not yet applied)
// Smart eligibility: CGPA, 10th, 12th, branch filter
require 'db.php';

$student_id = intval($_POST['student_id'] ?? 0);

// Fetch student academic info
$s = $conn->prepare("SELECT cgpa, tenth_percent, twelfth_percent, branch, is_placed FROM Student WHERE student_id=?");
$s->bind_param("i", $student_id);
$s->execute();
$student = $s->get_result()->fetch_assoc();

if (!$student) { echo json_encode([]); exit; }

// If placed, return empty (blocked)
if ($student['is_placed']) {
    echo json_encode(['placed' => true, 'jobs' => []]);
    exit;
}

$cgpa    = $student['cgpa'];
$tenth   = $student['tenth_percent'];
$twelfth = $student['twelfth_percent'];
$branch  = $student['branch'];

// LEFT JOIN to exclude already-applied jobs
// Branch filter: allowed_branch is empty OR matches student branch
$sql = "
    SELECT J.*, C.company_name, C.industry, C.location AS c_location,
           C.is_approved,
           COALESCE(A.status, '') AS applied_status
    FROM   Job J
    INNER  JOIN Company C ON J.company_id = C.company_id
    LEFT   JOIN Application A ON A.job_id = J.job_id AND A.student_id = ?
    WHERE  J.is_active = 1
      AND  C.is_approved = 1
      AND  A.application_id IS NULL
      AND  J.min_cgpa    <= ?
      AND  J.min_tenth   <= ?
      AND  J.min_twelfth <= ?
      AND  (J.allowed_branch = '' OR J.allowed_branch IS NULL OR J.allowed_branch LIKE ?)
    ORDER  BY J.package_lpa DESC
";

$branchFilter = '%' . $branch . '%';
$stmt = $conn->prepare($sql);
$stmt->bind_param("iddds", $student_id, $cgpa, $tenth, $twelfth, $branchFilter);
$stmt->execute();
$res  = $stmt->get_result();

$jobs = [];
while ($row = $res->fetch_assoc()) {
    $jobs[] = $row;
}
echo json_encode(['placed' => false, 'jobs' => $jobs]);
$conn->close();
?>
