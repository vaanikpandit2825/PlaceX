<?php
// save_resume.php
require 'db.php';
$id    = intval($_POST['student_id']    ?? 0);
$sk    = trim($_POST['skills']          ?? '');
$exp   = trim($_POST['experience']      ?? '');
$proj  = trim($_POST['projects']        ?? '');
$certs = trim($_POST['certifications']  ?? '');

// Upsert resume
$stmt = $conn->prepare("INSERT INTO Resume(student_id,skills,experience,projects,certifications) VALUES(?,?,?,?,?)
    ON DUPLICATE KEY UPDATE skills=VALUES(skills),experience=VALUES(experience),projects=VALUES(projects),certifications=VALUES(certifications)");
$stmt->bind_param("issss", $id, $sk, $exp, $proj, $certs);
$ok = $stmt->execute();

// Also sync skills field in Student
if ($ok) {
    $s2 = $conn->prepare("UPDATE Student SET skills=? WHERE student_id=?");
    $s2->bind_param("si", $sk, $id);
    $s2->execute();
    echo "Success";
} else {
    echo "Save failed: " . $conn->error;
}
$conn->close();
?>
