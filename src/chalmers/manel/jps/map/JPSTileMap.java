package chalmers.manel.jps.map;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import chalmers.manel.jps.exceptions.MapNotFoundInMapsInfoXML;

public class JPSTileMap {

	private int loadedMap; //number of the map

	//Data stored in tiles
	private int widthTiles; //width of the map in tiles
	private int heightTiles; //height of the map in tiles
	private int sizeTile; // size of each tile in pixels
	private boolean[] canWalk; // true -> possible to get there
	private int[] costWalk; // how much time do you spend walking speed=speed/costWalk[yourpos]
	private int[] renderTiles; //Which tile to each position of the map
	private int numTiles; //Number of tiles to render
	
	//Data stored in pixels
	private  int widthPixels;
	private int heightPixels;
	
	public JPSTileMap(int map) throws MapNotFoundInMapsInfoXML{
		this.loadedMap = map;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream is = getClass().getClassLoader().getResourceAsStream("maps/info/mapsInfo.xml");
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList listOfMaps = doc.getElementsByTagName("map");
			int totalMaps = listOfMaps.getLength();
			if(map > totalMaps) throw new MapNotFoundInMapsInfoXML("Map idex out of range");
			
			Node parseMap = listOfMaps.item(map);
			if(parseMap.getNodeType() == Node.ELEMENT_NODE){
				Element myMap = (Element) parseMap;
				
				//Parse Tiles element of the map
				NodeList widthTilesList = myMap.getElementsByTagName("widthTiles");
                Element widthTiles = (Element)widthTilesList.item(0);

                NodeList textFNList = widthTiles.getChildNodes();
                System.out.println("width Tiles : " + 
                       ((Node)textFNList.item(0)).getNodeValue().trim());
                this.widthTiles = Integer.parseInt(((Node)textFNList.item(0)).getNodeValue().trim());
                
                
                NodeList heightTilesList = myMap.getElementsByTagName("heightTiles");
                Element heightTiles = (Element)heightTilesList.item(0);

                textFNList = heightTiles.getChildNodes();
                System.out.println("height Tiles : " + 
                       ((Node)textFNList.item(0)).getNodeValue().trim());
                this.heightTiles = Integer.parseInt(((Node)textFNList.item(0)).getNodeValue().trim());
                
                
                NodeList sizeTilesList = myMap.getElementsByTagName("sizeTiles");
                Element sizeTiles = (Element)sizeTilesList.item(0);

                textFNList = sizeTiles.getChildNodes();
                System.out.println("sizeTiles : " + 
                       ((Node)textFNList.item(0)).getNodeValue().trim());
                this.sizeTile = Integer.parseInt(((Node)textFNList.item(0)).getNodeValue().trim());

                NodeList numTilesList = myMap.getElementsByTagName("numTiles");
                Element numTiles = (Element)numTilesList.item(0);

                textFNList = numTiles.getChildNodes();
                System.out.println("numTiles : " + 
                       ((Node)textFNList.item(0)).getNodeValue().trim());
                this.numTiles = Integer.parseInt(((Node)textFNList.item(0)).getNodeValue().trim());
                
                //set pixels variables
                this.heightPixels = this.heightTiles*this.sizeTile;
                this.widthPixels = this.widthTiles*this.sizeTile;
                this.canWalk = new boolean[this.widthTiles*this.heightTiles];
                this.costWalk = new int[this.widthTiles*this.heightTiles];
                this.renderTiles = new int[this.widthTiles*this.heightTiles];
                
                
                //parse resources
                NodeList movementMapList = myMap.getElementsByTagName("movementMap");
                Element movementMap = (Element)movementMapList.item(0);

                textFNList = movementMap.getChildNodes();
                System.out.println("movementMap : " + 
                		((Node)textFNList.item(0)).getNodeValue().trim());
                String movementMapPath = ((Node)textFNList.item(0)).getNodeValue().trim();

                BufferedInputStream bisCanWalk = new BufferedInputStream(
                		getClass().getClassLoader().getResourceAsStream(
                				"maps/map_" + map + "/" + movementMapPath));
                int l = bisCanWalk.read();
                int pos = 0;
                while(l != -1){
                	if(l == 42) this.canWalk[pos] = false;
                	if(l == 32) this.canWalk[pos] = true;
                	if(l == 10 || l == 13) pos--;
                	pos++;
                	l = bisCanWalk.read();
                }

                NodeList costMapList = myMap.getElementsByTagName("costMap");
                Element costMap = (Element)costMapList.item(0);
                textFNList = costMap.getChildNodes();
                System.out.println("costMap : " + 
                       ((Node)textFNList.item(0)).getNodeValue().trim());
                String costMapPath = ((Node)textFNList.item(0)).getNodeValue().trim();
                BufferedInputStream bisCostMap = new BufferedInputStream(
                		getClass().getClassLoader().getResourceAsStream(
                				"maps/map_" + map + "/" + costMapPath));
                int c = bisCostMap.read();
                int cPos = 0;
                while(c != -1){
                	if(c == 10 || c == 13) 
                		cPos--;
                	else
                		costWalk[cPos]=c-48;
                	cPos++;
                	c = bisCostMap.read();
                }

                NodeList tileMapList = myMap.getElementsByTagName("tileMap");
                Element tileMap = (Element)tileMapList.item(0);
                textFNList = tileMap.getChildNodes();
                System.out.println("tileMap : " + 
                       ((Node)textFNList.item(0)).getNodeValue().trim());
                String tileMapPath = ((Node)textFNList.item(0)).getNodeValue().trim();
                
                BufferedInputStream bisRenderTiles = new BufferedInputStream(
                		getClass().getClassLoader().getResourceAsStream(
                				"maps/map_" + map + "/" + tileMapPath));
                int t = bisRenderTiles.read();
                int tPos = 0;
                this.numTiles = -1;
                while(t != -1){
                	if(t == 10 || t == 13) 
                		tPos--;
                	else
                		renderTiles[tPos]=t-48;
                	if (t-48 > this.numTiles)
                		this.numTiles = t-48;
                	tPos++;
                	t = bisRenderTiles.read();
                }
                System.out.println("Map loaded");
			}
		} 
		catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getLoadedMap() {
		return loadedMap;
	}

	public int getWidthTiles() {
		return widthTiles;
	}

	public int getHeightTiles() {
		return heightTiles;
	}

	public int getSizeTile() {
		return sizeTile;
	}

	public boolean getCanWalk(int position) {
		return canWalk[position];
	}
	public boolean getCanWalk(int row, int col) {
		return canWalk[matToVecPos(row, col)];
	}
	public boolean getCanWalkPixels(float row, float col){
		int x = (int) (row/this.getSizeTile());
		int y = (int) (col/this.getSizeTile());
		return getCanWalk(x, y);
	}

	public int getCostWalk(int position) {
		return costWalk[position];
	}

	public int getCostWalk(int row, int col) {
		return costWalk[matToVecPos(row, col)];
	}
	
	public int getRenderTiles(int position) {
		return renderTiles[position];
	}

	public int getRenderTiles(int row, int col) {
		return renderTiles[matToVecPos(row, col)];
	}
	
	public int getNumTiles() {
		return numTiles;
	}

	public int getWidthPixels() {
		return widthPixels;
	}

	public int getHeightPixels() {
		return heightPixels;
	}

	private int matToVecPos(int row, int col){
		return row*this.widthTiles+col;
	}
}
