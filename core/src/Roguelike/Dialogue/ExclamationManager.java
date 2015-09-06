package Roguelike.Dialogue;

import com.badlogic.gdx.utils.Array;

public class ExclamationManager
{
	public ExclamationEventWrapper seePlayer;
	public ExclamationEventWrapper seeAlly;
	public ExclamationEventWrapper seeEnemy;
	public ExclamationEventWrapper lowHealth;
	public ExclamationEventWrapper victory;

	private static class ExclamationEventWrapper
	{
		public Array<ExclamationGroupWrapper> groups = new Array<ExclamationGroupWrapper>();
		public int cooldown;
	}

	private static class ExclamationGroupWrapper
	{
		public String condition;
		public Array<ExclamationWrapper> children = new Array<ExclamationWrapper>();
	}

	private static class ExclamationWrapper
	{
		public String condition;
		public String soundKey;
		public String text;
	}
}
