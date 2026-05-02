<?php
// recruiter_dashboard.php
require 'db.php';
$recruiter_id = intval($_POST['recruiter_id'] ?? 0);
$company_id   = intval($_POST['company_id']   ?? 0);

// Company approval status
$cs = $conn->prepare("SELECT company_name, is_approved FROM Company WHERE company_id=?");
$cs->bind_param("i", $company_id);
$cs->execute();
$company = $cs->get_result()->fetch_assoc();

if (!$company || $company['is_approved'] != 1) {
    echo json_encode(['approved' => false, 'company_name' => $company['company_name'] ?? '']);
    exit;
}

// Jobs posted by this company
$js = $conn->prepare("
    SELECT J.job_id, J.title, J.package_lpa, J.min_cgpa, J.location,
           J.job_type, J.deadline, J.is_active, J.required_skills,
           COUNT(A.application_id) AS applicant_count
    FROM   Job J
    LEFT   JOIN Application A ON A.job_id = J.job_id
    WHERE  J.company_id = ?
    GROUP  BY J.job_id
    ORDER  BY J.created_at DESC
");
$js->bind_param("i", $company_id);
$js->execute();
$jobs_res = $js->get_result();
$jobs = [];
while ($r = $jobs_res->fetch_assoc()) $jobs[] = $r;

// Total stats
$total_apps  = $conn->query("SELECT COUNT(*) FROM Application A INNER JOIN Job J ON A.job_id=J.job_id WHERE J.company_id=$company_id")->fetch_row()[0];
$shortlisted = $conn->query("SELECT COUNT(*) FROM Application A INNER JOIN Job J ON A.job_id=J.job_id WHERE J.company_id=$company_id AND A.status='Shortlisted'")->fetch_row()[0];
$selected    = $conn->query("SELECT COUNT(*) FROM Application A INNER JOIN Job J ON A.job_id=J.job_id WHERE J.company_id=$company_id AND A.status='Selected'")->fetch_row()[0];

echo json_encode([
    'approved'      => true,
    'company_name'  => $company['company_name'],
    'jobs'          => $jobs,
    'total_apps'    => $total_apps,
    'shortlisted'   => $shortlisted,
    'selected'      => $selected
]);
$conn->close();
?>
