<?php
// post_job.php
require 'db.php';
$company_id     = intval($_POST['company_id']     ?? 0);
$recruiter_id   = intval($_POST['recruiter_id']   ?? 0);
$title          = trim($_POST['title']            ?? '');
$description    = trim($_POST['description']      ?? '');
$package_lpa    = floatval($_POST['package_lpa']  ?? 0);
$min_cgpa       = floatval($_POST['min_cgpa']     ?? 6.0);
$min_tenth      = floatval($_POST['min_tenth']    ?? 60.0);
$min_twelfth    = floatval($_POST['min_twelfth']  ?? 60.0);
$req_skills     = trim($_POST['required_skills']  ?? '');
$branch         = trim($_POST['allowed_branch']   ?? '');
$location       = trim($_POST['location']         ?? '');
$job_type       = trim($_POST['job_type']         ?? 'Full Time');
$deadline       = trim($_POST['deadline']         ?? '');

if (!$title || !$company_id) { echo "Missing required fields"; exit; }

// Verify company is approved
$cs = $conn->prepare("SELECT is_approved FROM Company WHERE company_id=?");
$cs->bind_param("i", $company_id);
$cs->execute();
$co = $cs->get_result()->fetch_assoc();
if (!$co || $co['is_approved'] != 1) { echo "Company not approved yet"; exit; }

$stmt = $conn->prepare("INSERT INTO Job(title,description,package_lpa,min_cgpa,min_tenth,min_twelfth,required_skills,allowed_branch,location,job_type,deadline,company_id,recruiter_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
$stmt->bind_param("ssddddsssssii", $title,$description,$package_lpa,$min_cgpa,$min_tenth,$min_twelfth,$req_skills,$branch,$location,$job_type,$deadline,$company_id,$recruiter_id);
echo $stmt->execute() ? "Success" : "Failed: " . $conn->error;
$conn->close();
?>
