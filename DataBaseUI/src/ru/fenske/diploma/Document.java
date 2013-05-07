package ru.fenske.diploma;


public class Document {
	
	private Integer id;
	private String name;
	private String author;
	private String uri;
	
	public Document(Integer id, String name, String author, String uri) {
		this.id = id;
		this.name = name;
		this.author = author;
		this.uri = uri;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString() {
		return name;		
	}
}
