package hjsonpp.expand;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import mindustry.graphics.*;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

//Author: Photon_Gravity

public class AdjustableBeamNode extends BeamNode {
	/**Beam direction array. The contents of each row of the array are the x and y offsets by which the node will jump from itself to check for connections. Default value provided below mimicks the base beam node's orthognal lasers. Notably, order does not matter.*/
	public int[][] beamDirections = new int[][] {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};


	public AdjustableBeamNode(String name) {
		super(name);

	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		for(int i = 0; i < beamDirections.length; i++){
			int maxLen = range + size/2;
			Building dest = null;
			var dir = new Point2(beamDirections[i][0], beamDirections[i][1]).rotate(rotation);
			int dx = dir.x, dy = dir.y;
			int offset = size/2;
			for(int j = 1 + offset; j <= range + offset; j++){
				var other = world.build(x + j * dir.x, y + j * dir.y);

				//hit insulated wall
				if(other != null && other.isInsulated()){
					break;
				}

				if(other != null && other.block.hasPower && other.team == Vars.player.team() && !(other.block instanceof PowerNode)){
					maxLen = j;
					dest = other;
					break;
				}
			}

			Drawf.dashLine(Pal.placing,
					x * tilesize + dx * (tilesize * size / 2f + 2),
					y * tilesize + dy * (tilesize * size / 2f + 2),
					x * tilesize + dx * (maxLen) * tilesize,
					y * tilesize + dy * (maxLen) * tilesize
			);

			if(dest != null){
				Drawf.square(dest.x, dest.y, dest.block.size * tilesize/2f + 2.5f, 0f);
			}
		}
	}


	public class AdjustableNodeBuild extends BeamNodeBuild {
		public Building[] links = new Building[beamDirections.length];
		public Tile[] dests = new Tile[beamDirections.length];
		public int lastChange = -2;

		@Override
		public void draw(){
			super.draw();

			if(Mathf.zero(Renderer.laserOpacity)) return;

			Draw.z(Layer.power);
			Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
			Draw.alpha(Renderer.laserOpacity);
			float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

			for(int i = 0; i < beamDirections.length; i ++){
				if(dests[i] != null && links[i].wasVisible && (!(links[i].block instanceof BeamNode node) ||
						(links[i].tileX() != tileX() && links[i].tileY() != tileY()) ||
						(links[i].id > id && range >= node.range) || range > node.range)){

					int dst = Math.max(Math.abs(dests[i].x - tile.x),  Math.abs(dests[i].y - tile.y));
					//don't draw lasers for adjacent blocks
					if(dst > 1 + size/2){
						var point =  new Point2(beamDirections[i][0], beamDirections[i][1]).rotate(rotation);
						float poff = tilesize/2f;
						Drawf.laser(laser, laserEnd, x + poff*size*point.x, y + poff*size*point.y, dests[i].worldx() - poff*point.x, dests[i].worldy() - poff*point.y, w);
					}
				}
			}

			Draw.reset();
		}

		public void updateDirections(){
			for(int i = 0; i < beamDirections.length; i ++){
				var prev = links[i];
				var dir = new Point2(beamDirections[i][0], beamDirections[i][1]).rotate(rotation);
				links[i] = null;
				dests[i] = null;
				int offset = size/2;
				//find first block with power in range
				for(int j = 1 + offset; j <= range + offset; j++){
					var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

					//hit insulated wall
					if(other != null && other.isInsulated()){
						break;
					}

					//power nodes do NOT play nice with beam nodes, do not touch them as that forcefully modifies their links
					if(other != null && other.block.hasPower && other.block.connectedPower && other.team == team && !(other.block instanceof PowerNode)){
						links[i] = other;
						dests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
						break;
					}
				}

				var next = links[i];

				if(next != prev){
					//unlinked, disconnect and reflow
					if(prev != null){
						prev.power.links.removeValue(pos());
						power.links.removeValue(prev.pos());

						PowerGraph newgraph = new PowerGraph();
						//reflow from this point, covering all tiles on this side
						newgraph.reflow(this);

						if(prev.power.graph != newgraph){
							//reflow power for other end
							PowerGraph og = new PowerGraph();
							og.reflow(prev);
						}
					}

					//linked to a new one, connect graphs
					if(next != null){
						power.links.addUnique(next.pos());
						next.power.links.addUnique(pos());

						power.graph.addGraph(next.power.graph);
					}
				}
			}
		}
	}
}
