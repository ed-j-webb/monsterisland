package net.edwebb.jim.model.events;

import java.awt.Point;
import java.util.List;

import net.edwebb.jim.model.MapModel;

public class NoteChangeEvent extends MapSquareChangeEvent {

	private String oldNote;
	private String newNote;
	
	public NoteChangeEvent(MapModel model, Point square, String oldNote, String newNote) {
		this(model, square, oldNote, newNote, null);
	}
	
	public NoteChangeEvent(MapModel model, Point square, String oldNote, String newNote, List<MapChangeEvent>subEvents) {
		super(model, MAP_CHANGE_TYPE.NOTE, square, subEvents);
		this.oldNote = oldNote;
		this.newNote = newNote;
	}

	public String getOldNote() {
		return oldNote;
	}

	public String getNewNote() {
		return newNote;
	}

	@Override
	public String toString() {
		if (oldNote == null) {
			return "(" + square.y + "," + square.x + ") add note " + newNote;
		} else if (newNote == null) {
			return "(" + square.y + "," + square.x + ") remove note " + oldNote;
		} else {
			return "(" + square.y + "," + square.x + ") change note from " + oldNote + " to " + newNote;
		}
	}
}
