package com.cs_499.capstone

import java.util.ArrayList

val TAG: String = "BST"

class Node(var event: DBHelper.UserEvent) {
    var left: Node? = null
    var right: Node? = null
}

// creates BST, type determines in order by start date or event name
class BinarySearchTree(var type: Int) {
    var root: Node? = null
    var events: ArrayList<DBHelper.UserEvent> = ArrayList()


    fun insert(value: DBHelper.UserEvent) {
        root = insertRecursive(root, value)
    }

    // insert in order by startDate
    private fun insertRecursive(current: Node?, value: DBHelper.UserEvent): Node {
        if (current == null) {
            return Node(value)
        }

        if (type == 0) {


            if (value.startDate <= current.event.startDate) {
                current.left = insertRecursive(current.left, value)
            } else if (value.startDate > current.event.startDate) {
                current.right = insertRecursive(current.right, value)
            }


        // sort based on event title
        } else {
            if (value.discipline <= current.event.discipline) {
                current.left = insertRecursive(current.left, value)
            } else if (value.discipline > current.event.discipline) {
                current.right = insertRecursive(current.right, value)
            }
        }
        return current
    }

    // Tree traversal
    fun inOrderTraversal(node: Node?): List<DBHelper.UserEvent> {

        if (node!= null) {
            inOrderTraversal(node.left)
            events.add(node.event)

            inOrderTraversal(node.right)
        }

        return events
    }

}

