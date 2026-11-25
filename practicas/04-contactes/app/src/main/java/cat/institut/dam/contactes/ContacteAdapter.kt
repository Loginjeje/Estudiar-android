package cat.institut.dam.contactes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * ContacteAdapter — connecta la llista de contactes amb el RecyclerView.
 *
 * Què passa en prémer una fila o el favorit ho decideix la Activity.
 */
class ContacteAdapter(
    private val onContacteClick: (Contacte) -> Unit,
    private val onFavoritClick: (Contacte) -> Unit
) : RecyclerView.Adapter<ContacteAdapter.ContacteViewHolder>() {

    private var contactes: List<Contacte> = emptyList()

    fun actualitza(novs: List<Contacte>) {
        contactes = novs
        notifyDataSetChanged()
    }

    /** Guarda les referències a les vistes per no fer findViewById a cada bind. */
    class ContacteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInicials: TextView = itemView.findViewById(R.id.tvInicials)
        val tvNom: TextView = itemView.findViewById(R.id.tvNom)
        val tvCiutat: TextView = itemView.findViewById(R.id.tvCiutat)
        val tvCorreu: TextView = itemView.findViewById(R.id.tvCorreu)
        val chkFavorit: CheckBox = itemView.findViewById(R.id.chkFavorit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContacteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacte, parent, false)
        return ContacteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ContacteViewHolder, position: Int) {
        val contacte = contactes[position]
        holder.tvInicials.text = contacte.inicials
        holder.tvNom.text = contacte.nom
        holder.tvCiutat.text = contacte.ciutat
        holder.tvCorreu.text = contacte.email

        // Sense això, en reciclar la fila es dispararia el listener de l'anterior contacte
        holder.chkFavorit.setOnCheckedChangeListener(null)
        holder.chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)
        holder.chkFavorit.setOnCheckedChangeListener { _, _ -> onFavoritClick(contacte) }

        holder.itemView.setOnClickListener { onContacteClick(contacte) }
    }

    override fun getItemCount(): Int = contactes.size
}
