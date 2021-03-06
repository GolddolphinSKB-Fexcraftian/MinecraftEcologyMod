package ecomod.client.advancements.util;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListenersBase<I extends ICriterionInstance>
{
	private final PlayerAdvancements playerAdvancements;
	private final Set<ICriterionTrigger.Listener<I>> listeners = new HashSet<>();
	
	public ListenersBase(PlayerAdvancements playerAdvancementsIn)
    {
        this.playerAdvancements = playerAdvancementsIn;
    }
	
	public boolean isEmpty()
    {
        return this.listeners.isEmpty();
    }
	
	public void add(ICriterionTrigger.Listener<I> listener)
    {
        this.listeners.add(listener);
    }
	
	public void remove(ICriterionTrigger.Listener<I> listener)
    {
        this.listeners.remove(listener);
    }
	
	
	public void trigger(EntityPlayerMP player, Object...args)
	{
		List<ICriterionTrigger.Listener<I>> list = null;
		
		 for (ICriterionTrigger.Listener<I> listener : this.listeners)
         {
			 if (((ITestable) listener.getCriterionInstance()).test(player, args))
             {
                 if (list == null)
                 {
                     list = new ArrayList<>();
                 }

                 list.add(listener);
             }
         }
		 
		 if (list != null)
         {
             for (ICriterionTrigger.Listener<I> listener1 : list)
             {
                 listener1.grantCriterion(this.playerAdvancements);
             }
         }
	}
}
