package Roguelike.Ability;

import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 15-Dec-15.
 */
public class AbilityTree
{
	public AbilityStage root;
	public AbilityStage current;

	public AbilityTree()
	{}

	public AbilityTree(IAbility ability)
	{
		root = new AbilityStage( ability );
		current = root;

		ability.setTree(this);
	}

	public void parse(XmlReader.Element xml )
	{}


	public static AbilityTree load( XmlReader.Element xml )
	{
		AbilityTree tree = new AbilityTree(  );

		tree.parse( xml );

		return tree;
	}

	public static class AbilityStage
	{
		public IAbility current;

		public int level = 1;
		public int expToNextLevel = 100;
		public int exp;

		public AbilityStage branch1;
		public AbilityStage branch2;

		public AbilityStage()
		{}

		public AbilityStage(IAbility ability)
		{
			current = ability;
		}

		public void gainExp(int _exp)
		{
			if (level == 10) { return; }

			exp += _exp;

			if (exp >= expToNextLevel)
			{
				level++;
				exp -= expToNextLevel;
				expToNextLevel += level * 50;
			}
		}
	}
}
