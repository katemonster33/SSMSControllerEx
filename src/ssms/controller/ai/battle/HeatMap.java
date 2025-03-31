/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.controller.ai.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author Malte Schulze
 */
public class HeatMap implements Callable<Boolean> {
    static private class MapCell {
        float heat = 0;
        public void reset() {
            heat = 0;
        }
        public float pushHeat(float heat) {
            return this.heat += heat;
        }
        public void capHeat() {
            if ( heat < 0 ) heat = 0;
        }
    }
    
    private final MapCell[][] map;
    private final MapCell[] mapIndexed;
    private float realWidth, realHeight, mapScale;
    private List<HeatProducer> heatProducers;
    private final Map<Integer,List<int[]>> radialOffsets = new HashMap<>();
    
    public HeatMap(float realWidth, float realHeight, float mapScale) {
        int width = (int)Math.ceil(realWidth * mapScale);
        int height = (int)Math.ceil(realHeight * mapScale);
        map = new MapCell[width][height];
        mapIndexed = new MapCell[width * height];
        for ( int i = 0; i < width; i++ ) {
            for ( int j = 0; j < height; j++ ) {
                map[i][j] = new MapCell();
                mapIndexed[i+j*width] = map[i][j];
            }
        }
        heatProducers = new ArrayList<>();
        this.realWidth = realWidth;
        this.realHeight = realHeight;
        this.mapScale = mapScale;
    }
    
    public int getX(float x) {
        return Math.round(x * mapScale);
    }
    
    public int getY(float y) {
        return Math.round(y * mapScale);
    }
    
    public int getDistance(float d) {
        return Math.round(d * mapScale);
    }
    
    public float getRealX(int x) {
        return (x + 0.5f) / mapScale;
    }
    
    public float getRealY(int y) {
        return (y + 0.5f) / mapScale;
    }
    
    public int getRealDistance(float d) {
        return (int)(d / mapScale);
    }
    
    public void setup(Collection<HeatProducer> heatProducers) {
        this.heatProducers.clear();
        this.heatProducers.addAll(heatProducers);
    }
    
    private List<int[]> calculateRadialOffsets(int r) {
        List<int[]> points = new ArrayList<>();
        int outerRadius = r*r; int innerRadius = (r-1)*(r-1);
        
        for (int i = 0; i <= r; i++) {
            for (int j = 0; j <= +r; j++ ) {
                int distanceSquared = j*j + i*i;
                if ( distanceSquared > innerRadius && distanceSquared <= outerRadius ) {
                    points.add(new int[]{i,j,r});
                    if ( i != 0 && j != 0 ) {
                        points.add(new int[]{-i,j,r});
                        points.add(new int[]{i,-j,r});
                        points.add(new int[]{-i,-j,r});
                    } else {
                        points.add(new int[]{-i,-j,r});
                    }
                }
            }
        }
        
        return points;
    }

    @Override
    public Boolean call() throws Exception {
        for ( MapCell cell : mapIndexed ) {
            cell.reset();
        }
        for ( HeatProducer hp : heatProducers ) {
            int x = getX(hp.getX()), y = getY(hp.getY());
            map[x][y].pushHeat(hp.getHeatAtDistance(0f));
            for ( int r = getDistance(hp.getMaxHeatRadius()); r > 0; r-- ) {
                //TODO could be stored as one continuos array
                List<int[]> offsets = radialOffsets.get(r);
                if ( offsets == null ) {
                    offsets = calculateRadialOffsets(r);
                }
                for ( int[] p : offsets ) {
                    map[x+p[0]][y+p[1]].pushHeat(hp.getHeatAtDistance(getRealDistance(p[2])));
                }
            }
        }
        for ( MapCell cell : mapIndexed ) {
            cell.capHeat();
        }
        //Actors [individual ships, grouped ships]
        /*
        Strike Force=>
        regroup: restore flux and shields, bring all members close in a safe spot that is safe to reach. Used if a ship is in trouble or if the ships are split up.
        follow: Flies the group from one wypoint to the next and executes a follow up order if the last point was reached. Ships are kept together when navigating between points. 
            If the threat threshold in one of the paths sectors exceeds a limit the order is aborted.
        attack: Keeps an attack order on the enemy until the target is destroyed or the heat in the targets sector or in the groups sector exceeds a limit.
        find target: Identify suitable targets(slower than the group), explore map based on low threat and distance, first target found is set, set path to follow and attack order at end.
        
        Anchor=>
        Hover at the border of the conflict maintaining maximum firing distance and retreating backwards to restore shields/flux.
        If the anchor has escorts assigned then the escorts will use point defense weapons to erect a screen but stay away from the anchors front.
        Escorts with long reaching weapons will keep enemy flankers at bay, circling around the anchor but staying away from the frontal arc.
        If the anchor is venting the escorts block fire aggressively for the anchor including in the frontal arc.
        Escorts that aren't fighters retreat to the back of the anchor if they need to vent but not if the anchor itself is venting.
        */
        
        return true;
    }
}
