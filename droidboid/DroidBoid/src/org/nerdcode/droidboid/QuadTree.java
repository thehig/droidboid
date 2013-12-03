 package org.nerdcode.droidboid;

import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

class QuadTree
{
	protected float worldWidth;		//The width of the world
	protected float worldHeight;	//The height of the world
	
	protected int boidsToSubdivideAt = 5;		//Maximum number of Boids that can be in one Node
	protected int maxTreeDepth = 5;				//Maximum depth the tree can descend to. At this depth in the tree there can be more than the maximum number of pre-divide boids
	
	private QuadNode rootNode;
	
	public QuadTree(float worldWidth, float worldHeight)
	{
		this.worldHeight = worldHeight;
		this.worldWidth = worldWidth;
		
		rootNode = new QuadNode(null, 0, 0, 0);
	}
	
	public QuadTree(float worldWidth, float worldHeight, int boidsToSubdivideAt, int maxTreeDepth)
	{
		this(worldWidth, worldHeight);
		this.boidsToSubdivideAt = boidsToSubdivideAt;
		this.maxTreeDepth = maxTreeDepth;
	}
	
	public void addBoid(Boid boid)
	{
		rootNode.addBoid(boid);
	}	
	
	public void printQuadTree()
	{
		rootNode.printNode("root");
	}
	
	public int size()
	{
		return rootNode.countFlockSize();
	}
	
	public int getTreeDepth()
	{
		return rootNode.getMaxDepth();
	}
	
	public Vector<Boid> getFlock()
	{
		return rootNode.getFlock();
	}
	
	public Vector<Vector<Boid>> getLeafFlocks()
	{
		Vector<Vector<Boid>> returningFlock = new Vector<Vector<Boid>>();
		//Returns each leaf of boids as a vector, within the overall result vector
		//Vector<Vector<Boid>> flockOfFlocks = new Vector<Vector<Boid>>();
		//System.out.println("================================LEAFFLOCKS==========================");
		returningFlock = rootNode.collectLeafFlocks(returningFlock);
		/*
		System.out.println("Get Leaf Flocks contains " + returningFlock.size() + " flocks");
		String out = "";
		int sum = 0;
		for(Vector<Boid> flock : returningFlock)
		{
			out += " " + flock.size();
			sum += flock.size();
		}
		System.out.println("These contain flocks of " + out + " totalling " + sum + " boids");
		*/
		return returningFlock;
	}
	
	public void rebalance()
	{
		//TODO: Fix rebalance
		//rootNode.rebalance();
	}
	

	public void drawQuadTree(GL10 gl) 
	{
		rootNode.drawNodeBoundary(gl);	
	}

//======================================================================
//======================================================================
//=======================Quad Node Begins Here==========================
//======================================================================
//======================================================================

	
	
	class QuadNode {
		private QuadNode parentNode; // Used for calling back up the tree when an
										// item moves

		private int myDepth; // The depth in the tree of the current Node
		private float leftXPos, topYPos; // The X, Y of the top left of the
												// current Node
		private float rightXPos, bottomYPos; // The X, Y of the top left
														// of the current Node which
														// will be calculated in the
														// constructor
		
		private float xMidpoint, yMidpoint; // The X, Y of the midpoint of the Node
		private Vector<Boid> internalFlock; // The Flock of Boids contained in this
											// node

		private QuadNode NW, SW, NE, SE; // The subnodes

		protected QuadNode(QuadNode parentNode, int myDepth, float topLeftXPos,
				float topLeftYPos) {
			this.parentNode = parentNode;

			this.myDepth = myDepth;
			internalFlock = new Vector<Boid>();

			// Get the top left x, y
			this.leftXPos = topLeftXPos;
			this.topYPos = topLeftYPos;

			calculateBounds();
			
			configureNodeVertices();
		}
		
		public Vector<Vector<Boid>> collectLeafFlocks(Vector<Vector<Boid>> flockToAddTo) 
		{
			//Okay, so this function is supposed to append all the leaf nodes to the flock
			if(isLeafNode())
			{
				//If this is a leaf node, we want to add our internal flock into the collection
				//Ideally we want to return a Vector<Boid> of the flock
				
				//System.out.println("Flock to add to contains " + flockToAddTo.size() + " flocks at this point");
				flockToAddTo.add(internalFlock);
			}
			else
			{
				if(NW != null) NW.collectLeafFlocks(flockToAddTo);
				if(NE != null) NE.collectLeafFlocks(flockToAddTo);
				if(SW != null) SW.collectLeafFlocks(flockToAddTo);
				if(SE != null) SE.collectLeafFlocks(flockToAddTo);
			}
			return flockToAddTo;
		}

		private FloatBuffer nodeCoordsFBuffer;
		
		private void configureNodeVertices() 
		{
			float vertices[] = {leftXPos, bottomYPos, 0.0f,	//Left Bottom
					leftXPos, topYPos, 0.0f,		//Left, Top					
					rightXPos, topYPos, 0.0f,	//Right, Top
					rightXPos, bottomYPos, 0.0f	//Right, Bottom			
			};
			nodeCoordsFBuffer = Boid.makeFloatBuffer(vertices);			
		}

		protected void drawNodeBoundary(GL10 gl) 
		{
			
	        drawLines(gl);
	        
	        if(NW != null) NW.drawNodeBoundary(gl);
	        if(NE != null) NE.drawNodeBoundary(gl);
	        if(SW != null) SW.drawNodeBoundary(gl);
	        if(SE != null) SE.drawNodeBoundary(gl);
	        
		}
		
		protected void drawLines(GL10 gl)
		{
			gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);			 
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, nodeCoordsFBuffer); 
	        //gl.glPushMatrix();
	        //gl.glTranslatef(0, 0, -20);
	        gl.glLineWidth(2.0f);
	        gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 4);
	        //gl.glPopMatrix();
		}

		/**
		 * Rebalances the current Node and its' children
		 * @return Returns true if the Node should be nullified due to its' internal flock now being null.
		 */
		protected boolean rebalance() {
			//Rebalance the tree. This will be done after the Boids have been updated and will revalidate the trees structure
			if(isLeafNode())
			{
				//Stage 1. - Check each of the boids to see if they are still within the bounds of the node.
				
				Vector<Boid> outsideNode = new Vector<Boid>();
				for(Boid b : internalFlock)
				{
					if(nodeContains(b))
					{
						//If the node still contains the Boid, no further work needs to be done.
					}
					else
					{
						//If the node no longer contains this Boid, remove it from the flock and ask the parent node to readd it
						outsideNode.add(b);
					}
				}
				
				for(Boid b : outsideNode)
				{
					//System.out.println("Depth " + myDepth + " " + b + " is now outside this Node");
					internalFlock.remove(b);
					parentNode.addBoid(b);
				}
				
				if(internalFlock.size() == 0)
				{
					internalFlock = null;
					//if the internal flock is now null, this child Node should be nullified.
				}
			}
			else
			{
				//If this is not a leaf node, rebalance each of the children in turn.
				/*
				if(NW != null) 
					if(NW.rebalance())
						NW = null;
				
				if(NE != null) 
					if(NE.rebalance())
						NE = null;
				
				if(SW != null) 
					if(SW.rebalance())
						SW = null;
				
				if(SE != null) 
					if(SE.rebalance())
						SE = null;
				*/
				//TODO: Confirm that this rebalance doesn't need to be run more than once to validate the tree
				//Question, could this need to be run more than once?
				//			could this invalidate the content count of the Node?	-	No, the addBoid function takes care of that
			}
			
			//This rebalancing method only moves the Boids to their relevant new Node.
			//It does not take now-null branches into account.

			return(internalFlock == null);
			//If this returns true, this should in turn be nullified
			
		}

		public void calculateBounds() {
			// Calculate the bottom right x, y
			rightXPos = (float) (leftXPos + (worldWidth / (Math.pow(2, myDepth))));
			bottomYPos = (float) (topYPos + (worldHeight / (Math.pow(2, myDepth))));

			// Determine the midpoints of the current Node
			xMidpoint = leftXPos + (getNodeWidth() / 2);
			yMidpoint = topYPos + (getNodeHeight() / 2);
		}

		protected void printNode(String title) {
			if (isLeafNode()) {
				String out = title + ": \t\tDepth " + myDepth + " Count " + internalFlock.size() + " -- ";
				for (Boid b : internalFlock)
					out += b + " - ";
				System.out.println(out.subSequence(0, out.length() - 2));	//The -2 strips the trailing '- '
			}
			else
			{
				System.out.println(title + " Branch Node :: Contains " + countFlockSize() + " boids between " + leftXPos + ", " + topYPos + " and " + rightXPos + ", " + bottomYPos);
			}

			if (NW != null)
				NW.printNode(title + " NW");
			if (NE != null)
				NE.printNode(title + " NE");
			if (SW != null)
				SW.printNode(title + " SW");
			if (SE != null)
				SE.printNode(title + " SE");
		}

		// ======================Helper Functions===========================
		/**
		 * Does a perimiter check to see if the boid is contained within the current
		 * Node
		 * 
		 * @param boid
		 *            The Boid to check the constraints of
		 * @return True if the Node contains the Boid, otherwise False
		 */
		private boolean nodeContains(Boid boid) {
			return boid.xpos >= leftXPos && boid.xpos <= rightXPos
					&& boid.ypos >= topYPos && boid.ypos <= bottomYPos;
		}
		
		
		
		protected void addBoid(Boid boid)
		{
			//Stage 1. Does this node contain the boid in question. If it doesn't, no point in continuing
			if(nodeContains(boid))
			{
				//Stage 2. 	Is this node a leaf. If it is, add it here and balance. 
				//			If it's not, find the appropriate child Node and add it there
				if(isLeafNode())
				{
					//Note: If this is a leaf, the internalFlock will be valid
					internalFlock.add(boid);
					
					//Stage 3.	Check if the addition of this boid invalidates the Quadtree
					if(internalFlock.size() > boidsToSubdivideAt)
					{
						//Stage 4.	Check if we are at the 'bottom' of the tree
						if(myDepth < maxTreeDepth)
						{
							//Stage 5.	If we're not at the bottom of the tree, we need to subdivide
							//			If we are at the bottom of the tree, do nothing. Leave it here.
							subDivide();
						}
					}
				}
				else
				{
					//If this is not a leaf node, the internal flock has been invalidated.
					//Add the boid to the relevant child
					//Determine the sub quadrant of the boid
					boolean west = boid.xpos < xMidpoint;
					boolean north = boid.ypos < yMidpoint;
					
					//Add the boid to the relevant Node
					if(north && west) 
					{
						//System.out.println("\t\t\tAdding to NW");
						if(NW == null)
							NW = new QuadNode(this, myDepth + 1, leftXPos, topYPos);
						NW.addBoid(boid);
					}
					else if(north && !west) 
					{
						//System.out.println("\t\t\tAdding to NE");
						if(NE == null)
							NE = new QuadNode(this, myDepth + 1, xMidpoint, topYPos);
						NE.addBoid(boid);
					}
					else if(!north && west) 
					{
						//System.out.println("\t\t\tAdding to SW");
						if(SW == null)
							SW = new QuadNode(this, myDepth + 1, leftXPos, yMidpoint);
						SW.addBoid(boid);
					}
					else if(!north && !west) 
					{
						//System.out.println("\t\t\tAdding to SE");
						if(SE == null)
							SE = new QuadNode(this, myDepth + 1, xMidpoint, yMidpoint);
						SE.addBoid(boid);
					}
				}
			}
			else
			{
				//If this node does not contain this boid, add it to the parent.
				if(parentNode != null)
				{
					parentNode.addBoid(boid);
				}
			}
		}
		
		private void subDivide()
		{
			//This method is called when the internal flock within a Node reaches its capacity.
			//Stage 1.	We need to add each of the Nodes back into the children
			
			//Store the nodes
			Vector<Boid> subdividingBoids = internalFlock;
			//And invalidate the flock, indicating to the next addBoid procedure that this node is no longer a leaf node
			internalFlock = null;
			for(Boid b : subdividingBoids)
			{
				addBoid(b);
			}
		}

		protected float getNodeWidth() {
			return rightXPos - leftXPos;
		}

		protected float getNodeHeight() {
			return bottomYPos - topYPos;
		}

		protected int countFlockSize() 
		{
			if(isLeafNode())
			{
				return internalFlock.size();
			}
			else
			{
				int childrenSize = 0;
				if(NW != null) childrenSize += NW.countFlockSize();
				if(NE != null) childrenSize += NE.countFlockSize();
				if(SW != null) childrenSize += SW.countFlockSize();
				if(SE != null) childrenSize += SE.countFlockSize();
				
				return childrenSize;
			}
		}

		protected boolean isLeafNode() {
			// A node is a leaf node if it has no child nodes and it has a valid flock
			//If a Node has no children and an invalid flock it is in the process of subdividing
			return (NW == null && NE == null && SW == null && SE == null && internalFlock != null);
		}
		
		protected int getMaxDepth() 
		{
			if(isLeafNode())
			{
				return myDepth;
			}
			else
			{
				int nwD = 0, neD = 0, swD = 0, seD = 0;	//The depths of each of the children
				if(NW != null)
					nwD = NW.getMaxDepth();
				if(NE != null)
					neD = NE.getMaxDepth();
				if(SW != null)
					swD = SW.getMaxDepth();
				if(SE != null)
					seD = SE.getMaxDepth();
				
				int maxDepth = Integer.MIN_VALUE;
				if(nwD > maxDepth) maxDepth = nwD;
				if(neD > maxDepth) maxDepth = neD;
				if(swD > maxDepth) maxDepth = swD;
				if(seD > maxDepth) maxDepth = seD;

				return maxDepth;
			}
		}
		
		protected Vector<Boid> getFlock()
		{
			if(isLeafNode())
			{
				return internalFlock;
			}
			else
			{
				Vector<Boid> childrensFlock = new Vector<Boid>();
				if(NW != null)
					childrensFlock.addAll(NW.getFlock());
				if(NE != null)
					childrensFlock.addAll(NE.getFlock());
				if(SW != null)
					childrensFlock.addAll(SW.getFlock());
				if(SE != null)
					childrensFlock.addAll(SE.getFlock());
				
				return childrensFlock;
			}
		}

	}

}