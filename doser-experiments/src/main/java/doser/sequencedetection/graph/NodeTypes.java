package doser.sequencedetection.graph;

/**
 * Enumeration to flag Nodes (for example if it is visited or not). Used for
 * Dijkstra algorithm
 */
enum NodeTypes {

	BLACK,

	GREY,

	RED,

	WHITE;
}