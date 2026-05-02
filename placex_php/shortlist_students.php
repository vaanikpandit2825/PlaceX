<?php
// shortlist_students.php
// Returns eligible students ranked by: skill_match score DESC, CGPA DESC
// Also lets recruiter shortlist a specific application
require 'db.php';

$action = trim($_POST['action'] ?? 'list'); // list | shortlist

if ($action === 'shortlist') {
    // Recruiter shortlists an application
    $app_id = intval($_POST['application_id'] ?? 0);
    $stmt   = $conn->prepare("UPDATE Application SET status='Shortlisted' WHERE application_id=?");
    $stmt->bind_param("i", $app_id);
    echo $stmt->execute() ? "Success" : "Failed: " . $conn->error;
    $conn->close(); exit;
}

// ── LIST: auto-rank students for a job ──────────────────────
$job_id = intval($_POST['job_id'] ?? 0);

// Fetch job details (for skill matching)
$js = $conn->prepare("SELECT required_skills, min_cgpa, min_tenth, min_twelfth FROM Job WHERE job_id=?");
$js->bind_param("i", $job_id);
$js->execute();
$job = $js->get_result()->fetch_assoc();
if (!$job) { echo json_encode([]); exit; }

$req_skills = array_filter(array_map('strtolower', array_map('trim', explode(',', $job['required_skills'] ?? ''))));

// Fetch all applicants for this job
$as = $conn->prepare("
    SELECT A.application_id, A.status,
           S.student_id, S.name, S.email, S.branch, S.cgpa,
           S.tenth_percent, S.twelfth_percent, S.skills, S.college
    FROM   Application A
    INNER  JOIN Student S ON A.student_id = S.student_id
    WHERE  A.job_id = ?
    ORDER  BY S.cgpa DESC
");
$as->bind_param("i", $job_id);
$as->execute();
$res = $as->get_result();

$students = [];
while ($row = $res->fetch_assoc()) {
    // Skill match scoring
    $student_skills = array_filter(array_map('strtolower', array_map('trim', explode(',', $row['skills'] ?? ''))));
    $match = 0;
    foreach ($req_skills as $rs) {
        foreach ($student_skills as $ss) {
            if (str_contains($ss, $rs) || str_contains($rs, $ss)) { $match++; break; }
        }
    }
    $total_req      = max(count($req_skills), 1);
    $skill_pct      = round($match * 100 / $total_req);
    $row['skill_match_pct'] = $skill_pct;
    $row['skill_match']     = "$match/" . count($req_skills) . " skills";
    $students[] = $row;
}

// Sort: skill_match DESC, CGPA DESC
usort($students, fn($a,$b) =>
    $b['skill_match_pct'] != $a['skill_match_pct']
        ? $b['skill_match_pct'] <=> $a['skill_match_pct']
        : $b['cgpa']            <=> $a['cgpa']
);

// Return top 10 with rank
$result = [];
foreach (array_slice($students, 0, 10) as $i => $s) {
    $s['rank'] = $i + 1;
    $result[]  = $s;
}
echo json_encode($result);
$conn->close();
?>
