package com.example.studentplacement.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.model.ApplicationDetail
import com.example.studentplacement.utils.ApiClient

class ApplicationAdapter(
    private var apps: List<ApplicationDetail>,
    private val onItemClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ApplicationAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title:   TextView = v.findViewById(R.id.tvAppJobTitle)
        val company: TextView = v.findViewById(R.id.tvAppCompany)
        val status:  TextView = v.findViewById(R.id.tvAppStatus)
        val pkg:     TextView = v.findViewById(R.id.tvAppPackage)
        val date:    TextView = v.findViewById(R.id.tvAppDate)
        val btnAccept: Button = v.findViewById(R.id.btnAcceptOffer)
        val tvTimeline: TextView? = try { v.findViewById(R.id.tvViewTimeline) } catch (e: Exception) { null }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false))

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = apps[pos]

        h.title.text   = a.jobTitle
        h.company.text = a.companyName
        h.pkg.text     = "₹ ${a.packageLpa} LPA"
        h.date.text    = "Applied: ${a.appliedAt.take(10)}"
        h.status.text  = a.status

        // 🎨 Status color
        h.status.setTextColor(
            when(a.status) {
                "Selected", "Offer Accepted" -> Color.parseColor("#4CAF50")
                "Interview Scheduled", "Interview Done" -> Color.parseColor("#FF9800")
                "Shortlisted" -> Color.parseColor("#2196F3")
                "Rejected" -> Color.parseColor("#F44336")
                else -> Color.parseColor("#64B5F6")
            }
        )


        h.btnAccept.visibility =
            if (a.status == "Selected") View.VISIBLE else View.GONE
        h.btnAccept.setOnClickListener {

            val ctx = h.itemView.context

            val req = object : StringRequest(
                Request.Method.POST,
                ApiClient.BASE_URL + "shortlist_students.php",

                {
                    Toast.makeText(ctx, "Offer Accepted 🎉", Toast.LENGTH_SHORT).show()

                    h.status.text = "Offer Accepted"
                    h.btnAccept.visibility = View.GONE
                },

                {
                    Toast.makeText(ctx, "Error", Toast.LENGTH_SHORT).show()
                }

            ) {
                override fun getParams() = mapOf(
                    "action" to "update_status",
                    "application_id" to a.applicationId.toString(),
                    "status" to "Offer Accepted"
                )
            }

            Volley.newRequestQueue(ctx).add(req)
        }


        h.itemView.setOnClickListener {
            onItemClick?.invoke(a.applicationId)
        }

        h.tvTimeline?.text = "Tap to view timeline →"
    }

    fun updateList(newList: List<ApplicationDetail>) {
        apps = newList
        notifyDataSetChanged()
    }
}