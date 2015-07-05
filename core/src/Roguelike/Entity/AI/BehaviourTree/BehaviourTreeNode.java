package Roguelike.Entity.AI.BehaviourTree;

import java.util.HashMap;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionClearValue;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionConvertTo;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionGetAllAbilities;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionGetAllVisible;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionMoveTo;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionPickClosest;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionPickRandom;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionProcessInput;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionSetValue;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionUseAbility;
import Roguelike.Entity.AI.BehaviourTree.Actions.ActionWait;
import Roguelike.Entity.AI.BehaviourTree.Conditionals.ConditionalCheckValue;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorDataScope;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorImport;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorInvert;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorRepeat;
import Roguelike.Entity.AI.BehaviourTree.Decorators.DecoratorSetState;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorAny;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorPriority;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorRandom;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorSequence;
import Roguelike.Entity.AI.BehaviourTree.Selectors.SelectorUntil;

import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class BehaviourTreeNode
{	
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	public void setData(String key, Object value)
	{
		if (value == null) { Data.remove(key); }
		else { Data.put(key, value); } 
	}
	
	//----------------------------------------------------------------------
	public Object getData(String key, Object fallback)
	{
		Object o = Data.get(key);
		return o != null ? o : fallback;
	}
	
	//----------------------------------------------------------------------
	public abstract BehaviourTreeState evaluate(Entity entity);
	
	//----------------------------------------------------------------------
	public abstract void cancel();
	
	//----------------------------------------------------------------------
	public abstract void parse(Element xmlElement);
	
	//endregion Public Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	protected static HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	
	//----------------------------------------------------------------------
	static
	{
		// Selectors
		ClassMap.put("Priority", SelectorPriority.class);
		ClassMap.put("Any", SelectorAny.class);
		ClassMap.put("Sequence", SelectorSequence.class);
		ClassMap.put("Random", SelectorRandom.class);
		ClassMap.put("Until", SelectorUntil.class);
		
		//Decorators
		ClassMap.put("DataScope", DecoratorDataScope.class);
		ClassMap.put("Import", DecoratorImport.class);
		ClassMap.put("Invert", DecoratorInvert.class);
		ClassMap.put("Repeat", DecoratorRepeat.class);
		ClassMap.put("SetState", DecoratorSetState.class);
		
		//Conditionals
		ClassMap.put("CheckValue", ConditionalCheckValue.class);
		
		//Actions
		ClassMap.put("ClearValue", ActionClearValue.class);
		ClassMap.put("ConvertTo", ActionConvertTo.class);
		ClassMap.put("GetAllVisible", ActionGetAllVisible.class);
		ClassMap.put("GetAllAbilities", ActionGetAllAbilities.class);
		ClassMap.put("MoveTo", ActionMoveTo.class);
		ClassMap.put("PickClosest", ActionPickClosest.class);
		ClassMap.put("PickRandom", ActionPickRandom.class);
		ClassMap.put("ProcessInput", ActionProcessInput.class);
		ClassMap.put("SetValue", ActionSetValue.class);
		ClassMap.put("UseAbility", ActionUseAbility.class);
		ClassMap.put("Wait", ActionWait.class);
	}
	
	//----------------------------------------------------------------------
	public BehaviourTreeContainer Parent;
	
	//----------------------------------------------------------------------
	protected BehaviourTreeState State;
	
	//----------------------------------------------------------------------
	public HashMap<String, Object> Data;
	
	//endregion Data
	//####################################################################//
}