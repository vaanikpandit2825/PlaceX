echo "NEW_FILE_WORKING";
exit();
header('Content-Type: application/json');
error_reporting(E_ALL);
ini_set('display_errors', 1); // 🔥 prevent <br> errors breaking JSON

require 'db.php';

// Helper function to safely fetch single value
function getVal($conn, $query) {
    $res = $conn->query($query);
    if ($res && $row = $res->fetch_row()) {
        return $row[0] ?? 0;
    }
    return 0;
}

// 🔥 Overview stats
$total_students  = getVal($conn, "SELECT COUNT(*) FROM Student");
$total_placed    = getVal($conn, "SELECT COUNT(*) FROM Student WHERE is_placed=1");
$total_jobs = getVal($conn, "SELECT COUNT(*) FROM Job");
$total_apps      = getVal($conn, "SELECT COUNT(*) FROM Application");
$total_companies = getVal($conn, "SELECT COUNT(*) FROM Company WHERE is_approved=1");
$pending_cos     = getVal($conn, "SELECT COUNT(*) FROM Company WHERE is_approved=0");
$avg_pkg    = getVal($conn, "SELECT ROUND(AVG(package_lpa),2) FROM Job");   

$placement_pct = ($total_students > 0)
    ? round(($total_placed * 100) / $total_students, 1)
    : 0;

// 🔥 Pending companies
$pending = [];
$p_res = $conn->query("
    SELECT C.company_id, C.company_name, C.industry, C.location,
           C.website, C.description,
           R.name AS recruiter_name, R.email AS recruiter_email
    FROM Company C
    LEFT JOIN Recruiter R ON R.company_id = C.company_id
    WHERE C.is_approved = 0
    ORDER BY C.company_id DESC
");

if ($p_res) {
    while ($r = $p_res->fetch_assoc()) {
        $pending[] = $r;
    }
}

// 🔥 Recent applications
$recent_apps = [];
$ra_res = $conn->query("
    SELECT A.application_id, A.status, A.applied_at,
           S.name AS student_name, S.branch, S.cgpa,
           J.title AS job_title, C.company_name
    FROM Application A
    INNER JOIN Student S ON A.student_id = S.student_id
    INNER JOIN Job J ON A.job_id = J.job_id
    INNER JOIN Company C ON J.company_id = C.company_id
    ORDER BY A.applied_at DESC
    LIMIT 10
");

if ($ra_res) {
    while ($r = $ra_res->fetch_assoc()) {
        $recent_apps[] = $r;
    }
}

// 🔥 Branch placement
$branch_placement = [];
$br_res = $conn->query("
    SELECT S.branch,
           COUNT(*) AS total,
           SUM(CASE WHEN S.is_placed=1 THEN 1 ELSE 0 END) AS placed,
           ROUND(SUM(CASE WHEN S.is_placed=1 THEN 1 ELSE 0 END)*100.0 / COUNT(*),1) AS pct
    FROM Student S
    GROUP BY S.branch
");

if ($br_res) {
    while ($r = $br_res->fetch_assoc()) {
        $branch_placement[] = $r;
    }
}

// 🔥 Top students
$top_students = [];
$top_res = $conn->query("
    SELECT S.name, S.branch, S.cgpa,
           COUNT(A.application_id) AS apps_count
    FROM Student S
    LEFT JOIN Application A ON A.student_id = S.student_id
    GROUP BY S.student_id
    ORDER BY S.cgpa DESC
    LIMIT 10
");

if ($top_res) {
    while ($r = $top_res->fetch_assoc()) {
        $top_students[] = $r;
    }
}

// 🔥 Final response
$response = [
    'status'            => 'success',
    'total_students'    => $total_students,
    'total_placed'      => $total_placed,
    'placement_pct'     => $placement_pct,
    'total_jobs'        => $total_jobs,
    'total_apps'        => $total_apps,
    'total_companies'   => $total_companies,
    'pending_cos'       => $pending_cos,
    'avg_package'       => $avg_pkg,
    'pending_companies' => $pending,
    'recent_apps'       => $recent_apps,
    'branch_placement'  => $branch_placement,
    'top_students'      => $top_students
];

echo json_encode($response);
$conn->close();
?>