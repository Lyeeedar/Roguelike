package Roguelike.Save;

import Roguelike.Ability.AbilityTree;

/**
 * Created by Philip on 16-Dec-15.
 */
public class SaveAbilityTree extends SaveableObject<AbilityTree>
{
	public String treePath;
	public SaveAbilityStage root;
	public String currentName;

	@Override
	public void store( AbilityTree obj )
	{
		treePath = obj.treePath;
		root = new SaveAbilityStage();
		root.store( obj.root );
		currentName = obj.current.current.getName();
	}

	@Override
	public AbilityTree create()
	{
		AbilityTree tree = new AbilityTree( treePath );
		writeData( tree );

		return tree;
	}

	public void writeData(AbilityTree tree)
	{
		tree.current = root.writeData( tree.root, currentName );
	}

	public static class SaveAbilityStage extends SaveableObject<AbilityTree.AbilityStage>
	{
		public int level;
		public int exp;
		public int expToNextLevel;

		public int cooldown;

		public SaveAbilityStage branch1;
		public SaveAbilityStage branch2;

		@Override
		public void store( AbilityTree.AbilityStage obj )
		{
			level = obj.level;
			exp = obj.exp;
			expToNextLevel = obj.expToNextLevel;
			cooldown = obj.current.getCooldown();

			if (obj.branch1 != null)
			{
				branch1 = new SaveAbilityStage();
				branch1.store( obj.branch1 );

				branch2 = new SaveAbilityStage();
				branch2.store( obj.branch2 );
			}
		}

		@Override
		public AbilityTree.AbilityStage create()
		{
			return null;
		}

		public AbilityTree.AbilityStage writeData( AbilityTree.AbilityStage stage, String currentName )
		{
			stage.level = level;
			stage.exp = exp;
			stage.expToNextLevel = expToNextLevel;
			stage.current.setCooldown( cooldown );

			AbilityTree.AbilityStage currentStage = stage.current.getName() == currentName || stage.current.getName().equals( currentName ) ? stage : null;
			if (stage.branch1 != null)
			{
				AbilityTree.AbilityStage temp = branch1.writeData( stage.branch1, currentName );
				if (temp != null) { currentStage = temp; }

				temp = branch2.writeData( stage.branch2, currentName );
				if (temp != null) { currentStage = temp; }
			}

			return currentStage;
		}
	}
}
