package com.dnio.flowwright.core.node

opaque type NodeId = String

object NodeId {

  val EndNodeId: NodeId = "end"

  def apply(id: String): NodeId = id

  extension (nodeId: NodeId) def asString: String = nodeId
}
