package lsedit;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

public class MyUndoableEdit extends AbstractUndoableEdit implements UndoableEdit {

	protected static Diagram getDiagram(LandscapeObject object)
	{
		return object.getDiagram();
	}

	public boolean canUndo()
	{
		return true;
	}

	public boolean canRedo()
	{
		return true;
	}

}

