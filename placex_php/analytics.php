<?php
// analytics.php — platform analytics + student personal stats
require 'db.php';
$student_id = intval($_POST['student_id'] ?? 0);

// Platform stats
$total_students  = $conn->query("SELECT COUNT(*) FROM Student")->fetch_row()[0];
$total_jobs      = $conn->query("SELECT COUNT(*) FROM Job WHERE is_active=1")->fetch_row()[0];
$total_apps      = $conn->query("SELECT COUNT(*) FROM Application")->fetch_row()[0];
$total_companies = $conn->query("SELECT COUNT(*) FROM Company WHERE is_approved=1")->fetch_row()[0];
$placed          = $conn->query("SELECT COUNT(DISTINCT student_id) FROM Application WHERE status='Selected'")->fetch_row()[0];
$avg_pkg         = $conn->query("SELECT ROUND(AVG(package_lpa),2) FROM Job WHERE is_active=1")->fetch_row()[0];
$max_pkg         = $conn->query("SELECT MAX(package_lpa) FROM Job WHERE is_active=1")->fetch_row()[0];
$above_avg       = $conn->query("SELECT COUNT(*) FROM Student WHERE cgpa > (SELECT AVG(cgpa) FROM Student)")->fetch_row()[0];

// My personal stats
$my_stmt = $conn->prepare("SELECT status FROM Application WHERE student_id=?");
$my_stmt->bind_param("i", $student_id);
$my_stmt->execute();
$my_res = $my_stmt->get_result();
$my_applied = $my_interview = $my_placed = $my_rejected = 0;
while ($r = $my_res->fetch_assoc()) {
    $my_applied++;
    if ($r['status'] === 'Selected')            $my_placed++;
    if ($r['status'] === 'Interview Scheduled' ||
        $r['status'] === 'Interview Done')      $my_interview++;
    if ($r['status'] === 'Rejected')            $my_rejected++;
}

// Application status breakdown (GROUP BY)
$status_res = $conn->query("SELECT status, COUNT(*) AS cnt FROM Application GROUP BY status ORDER BY cnt DESC");
$status_breakdown = [];
while ($r = $status_res->fetch_assoc()) $status_breakdown[] = $r;

// Company stats (JOIN + GROUP BY + AVG)
$co_res = $conn->query("
    SELECT C.company_name, COUNT(J.job_id) AS job_count,
           ROUND(AVG(J.package_lpa),2) AS avg_pkg
    FROM   Company C
    INNER  JOIN Job J ON C.company_id = J.company_id
    WHERE  J.is_active = 1
    GROUP  BY C.company_id
    ORDER  BY avg_pkg DESC
    LIMIT 7
");
$company_stats = [];
while ($r = $co_res->fetch_assoc()) $company_stats[] = $r;

// Branch breakdown
$br_res = $conn->query("SELECT branch, COUNT(*) AS cnt FROM Student GROUP BY branch ORDER BY cnt DESC");
$branch_breakdown = [];
while ($r = $br_res->fetch_assoc()) $branch_breakdown[] = $r;

// Placement % by branch (for admin-level insight)
$pl_res = $conn->query("
    SELECT S.branch,
           COUNT(DISTINCT S.student_id) AS total,
           COUNT(DISTINCT CASE WHEN A.status='Selected' THEN A.student_id END) AS placed,
           ROUND(COUNT(DISTINCT CASE WHEN A.status='Selected' THEN A.student_id END)
                 * 100.0 / NULLIF(COUNT(DISTINCT S.student_id),0),1) AS pct
    FROM   Student S
    LEFT   JOIN Application A ON A.student_id = S.student_id
    GROUP  BY S.branch
    ORDER  BY pct DESC
");
$placement_by_branch = [];
while ($r = $pl_res->fetch_assoc()) $placement_by_branch[] = $r;

// Top 5 students by CGPA
$top_res = $conn->query("SELECT name, branch, cgpa FROM Student ORDER BY cgpa DESC LIMIT 5");
$top_students = [];
while ($r = $top_res->fetch_assoc()) $top_students[] = $r;

echo json_encode([
    'total_students'     => $total_students,
    'total_jobs'         => $total_jobs,
    'total_apps'         => $total_apps,
    'total_companies'    => $total_companies,
    'placed'             => $placed,
    'avg_package'        => $avg_pkg,
    'max_package'        => $max_pkg,
    'above_avg_cgpa'     => $above_avg,
    'my_applied'         => $my_applied,
    'my_placed'          => $my_placed,
    'my_interview'       => $my_interview,
    'my_rejected'        => $my_rejected,
    'status_breakdown'   => $status_breakdown,
    'company_stats'      => $company_stats,
    'branch_breakdown'   => $branch_breakdown,
    'placement_by_branch'=> $placement_by_branch,
    'top_students'       => $top_students
]);
$conn->close();
?>
