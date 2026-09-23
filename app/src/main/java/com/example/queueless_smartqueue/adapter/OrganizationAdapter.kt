package com.example.queueless_smartqueue.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.queueless_smartqueue.databinding.ItemOrganizationBinding
import com.example.queueless_smartqueue.model.Organization

class OrganizationAdapter(
    private val orgs: List<Organization>,
    private val onOrgSelected: (Organization) -> Unit
) : RecyclerView.Adapter<OrganizationAdapter.OrgViewHolder>() {

    class OrgViewHolder(val binding: ItemOrganizationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrgViewHolder {
        val binding = ItemOrganizationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrgViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrgViewHolder, position: Int) {
        val org = orgs[position]
        holder.binding.tvEmoji.text = org.iconEmoji
        holder.binding.tvOrgName.text = org.name
        holder.binding.tvOrgAddress.text = org.address
        holder.binding.tvStatus.text = if (org.isOpen) "Open • ${org.activeCountersCount} counters active" else "Closed"

        holder.binding.btnViewServices.setOnClickListener {
            onOrgSelected(org)
        }
    }

    override fun getItemCount(): Int = orgs.size
}
