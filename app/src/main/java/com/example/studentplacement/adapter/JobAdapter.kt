package com.example.studentplacement.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentplacement.R
import com.example.studentplacement.activities.JobDetailActivity
import com.example.studentplacement.model.Job

class JobAdapter(
    private var jobs: List<Job>,
    private val onClick: (Job) -> Unit
) : RecyclerView.Adapter<JobAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title:   TextView = v.findViewById(R.id.tvJobTitle)
        val company: TextView = v.findViewById(R.id.tvCompanyName)
        val pkg:     TextView = v.findViewById(R.id.tvPackage)
        val loc:     TextView = v.findViewById(R.id.tvLocation)
        val type:    TextView = v.findViewById(R.id.tvJobType)
        val cgpa:    TextView = v.findViewById(R.id.tvMinCgpa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false))

    override fun getItemCount() = jobs.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val j = jobs[pos]
        h.title.text   = j.title
        h.company.text = j.companyName
        h.pkg.text     = "₹ ${j.packageLpa} LPA"
        h.loc.text     = "📍 ${j.location}"
        h.type.text    = j.jobType
        h.cgpa.text    = "Min CGPA: ${j.minCgpa}"
        h.itemView.setOnClickListener {
            val ctx = h.itemView.context
            ctx.startActivity(Intent(ctx, JobDetailActivity::class.java).apply {
                putExtra("job_id",      j.id)
                putExtra("company_id",  j.companyId)
                putExtra("job_title",   j.title)
                putExtra("company_name",j.companyName)
                putExtra("industry",    j.industry)
                putExtra("package_lpa", j.packageLpa)
                putExtra("location",    j.location)
                putExtra("job_type",    j.jobType)
                putExtra("deadline",    j.deadline)
                putExtra("description", j.description)
                putExtra("min_cgpa",    j.minCgpa)
                putExtra("min_tenth",   j.minTenth)
                putExtra("min_twelfth", j.minTwelfth)
            })
        }
    }

    fun updateList(newList: List<Job>) { jobs = newList; notifyDataSetChanged() }
}
