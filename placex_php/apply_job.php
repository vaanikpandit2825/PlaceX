<?php
// apply_job.php
require 'db.php';

$student_id = intval($_POST['student_id'] ?? 0);
$job_id     = intval($_POST['job_id']     ?? 0);

// ── Anti-cheat: check if already applied ────────────────────
$chk = $conn->prepare("SELECT 1 FROM Application WHERE student_id=? AND job_id=?");
$chk->bind_param("ii", $student_id, $job_id);
$chk->execute();
if ($chk->get_result()->num_rows > 0) { echo "Already applied"; exit; }

// ── System Behavior: block placed students ───────────────────
$ps = $conn->prepare("SELECT is_placed, cgpa, tenth_percent, twelfth_percent FROM Student WHERE student_id=?");
$ps->bind_param("i", $student_id);
$ps->execute();
$student = $ps->get_result()->fetch_assoc();

if (!$student) { echo "Student not found"; exit; }
if ($student['is_placed']) {
    echo "You are already placed and cannot apply to more companies";
    exit;
}

// ── Eligibility check ────────────────────────────────────────
$js = $conn->prepare("SELECT min_cgpa, min_tenth, min_twelfth FROM Job WHERE job_id=?");
$js->bind_param("i", $job_id);
$js->execute();
$job = $js->get_result()->fetch_assoc();

if (!$job) { echo "Job not found"; exit; }

if ($student['cgpa']            < $job['min_cgpa']    ||
    $student['tenth_percent']   < $job['min_tenth']   ||
    $student['twelfth_percent'] < $job['min_twelfth']) {
    echo "Not eligible"; exit;
}

// ── Insert application ───────────────────────────────────────
$ins = $conn->prepare("INSERT INTO Application(student_id, job_id, status) VALUES(?,?,'Applied')");
$ins->bind_param("ii", $student_id, $job_id);
if ($ins->execute()) {
    echo "Success";
} else {
    echo "Application failed: " . $conn->error;
}
$conn->close();
?>
