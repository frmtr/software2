package forest;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class ForestModel {

	/**
	 * 依存物（Observerデザインパターンの観測者）：Viewのインスタンスたちを束縛する。
	 * 良好（2010年7月25日）
	 */
	protected ArrayList<ForestView> dependents;

	private ArrayList<Node> nodes;

	public int leaf = 0;

	public final int height = 20;

	public final int distance = 30;

	public final long millis = 10;

	/**
	 * インスタンスを生成して初期化して応答する。
	 * 良好（2010年7月25日）
	 * @throws FileNotFoundException
	 */
	public ForestModel(File aFile) throws FileNotFoundException
	{
		super();
		//this.initialize(aFile);
		dependents = new ArrayList<ForestView>();
		nodes = new ArrayList<Node>();
		this.readFile(aFile);

		return;
	}



	/**
	 * 指定されたビューを依存物に設定する。
	 * @param aForestView このモデルの依存物となるビュー
	 *
	 */
	public void addDependent(ForestView aForestView)
	{
		dependents.add(aForestView);
		return;
	}

	/**
	 * モデルの内部状態が変化していたので、自分の依存物へupdateのメッセージを送信する。
	 */
	public void changed()
	{
		Iterator<ForestView> anIterator = dependents.iterator();
		while (anIterator.hasNext())
		{
			ForestView aForestView = anIterator.next();
			aForestView.update();
		}
		return;
	}

	public void readFile(File aFile) throws FileNotFoundException{

		FileInputStream fi = new FileInputStream(aFile);
		InputStreamReader is = new InputStreamReader(fi);
    	BufferedReader br = new BufferedReader(is);
		try {
	    	//読み込み行
    		String line;

    		//処理するデータの内容を保管
    		String currentReadContext = null;

    		//1行ずつ読み込みを行う
    		while ((line = br.readLine()) != null) {

    			//カンマで分割した内容を配列に格納する
    			//String[] data = line.split(",");

    	        //現在読み込んでいるデータを判別//次の行へ
    	        if(line.equals("trees:")){ currentReadContext = "trees"; continue;}
    	        if(line.equals("nodes:")){ currentReadContext = "nodes"; continue;}
    	        if(line.equals("branches:")){ currentReadContext = "branches"; continue;}

    	        //treeからの情報を取得
    	        //if(currentReadContext == "trees"){
    	        	//trees(line);
    	        //}
    	        if(currentReadContext.equals("nodes")){ node(line); }
    	        if(currentReadContext.equals("branches")){ branches(line); }

    		}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//最後にノードを名前順でソートする
		getNodes().sort(Comparator.comparing(Node::getName));
		return;
	}

	public void node(String line){
		//読み込んだデータを分割
		String[] data = line.split(", ");

		//初期座標
		int x = 10;
		int y = 30 + height*getNodes().size();
		Point aPoint = new Point(x,y);

		getNodes().add(new Node( Integer.parseInt(data[0]),data[1],aPoint));
		return;
	}

	public void branches(String line){
		String[] data = line.split(", ");
		Integer parentNodeNumber = Integer.parseInt(data[0]);
		Integer childNodeNumber = Integer.parseInt(data[1]);

		Node parentNode = getNodes().get(parentNodeNumber - 1);
		Node childNode =  getNodes().get(childNodeNumber - 1);

		//データ、左側のノードに、子ノードを割り当てる
		parentNode.getChildren().add(childNode);
		//データ、右側のノードに、親ノードを割り当てる
		childNode.getParents().add(parentNode);
		return;
	}


	/**
	 * 最初に読み込まれアニメーションのための準備をする
	 * 根ノードを探しmoveに渡す。
	 */
	public void animate(){

		//根ノードを取得
		for(Node node :getNodes() ){
			//根ノードを最初に描画
			if(node.getParents().size() == 0){
				//move(node);
				move(node,null);
			}
		}

		/*
		for(Node node :getNodes() ){
			//根ノードを最初に描画
			if(node.getParents().size() == 0){
				//print(node,-1);
			}
		}
		*/
	}

	/**
	 * 読み込まれたノードのView座標を根ノードから葉ノードまで再帰的に求める
	 * その過程をアニメーションする
	 * @param node 探索中のノード
	 * @param parent 探索中のノードの一つ上の親ノード。直属の親ノードの情報を持つ
	 */
	public void move(Node node,Node parent){
		//探索したノードをtrueにする
		node.setIsSearched(true);

		//モデル座標を設定
		node.setPoint(putNodeModelPoint(node,parent));
		//View座標を設定
		node.setViewPoint(node.getPoint());

		//経過をViewに伝え、アニメーションの時間を管理する
		processReport();

		//葉ノードときleafを加算
		countLeafNode(node,false);

		//ノードを名前順でソートする
		node.getChildren().sort(Comparator.comparing(Node::getName));

		//探索中のノードの子ノードが全て探索済みかを保持する
		boolean isAllOfChildNodeSearched = true;

		/*
		 * 自身が親ノードであり、子ノード探索前に子ノードがすでに全て探索済みであれば葉ノードと同じ表示方法をとる。つまり座標の更新を行わない.
		 * 調査前であれば更新する必要がある。isAllOfChildNodeSearchedはfalseになる。
		 */
		//自身が親ノードであるかを確認
		if(node.getChildren().size() !=0){
			//子ノード探索をする
			for(Node child : node.getChildren()){
				//探索前であれば条件を満たさない
				if(!child.getIsSearched()){
					isAllOfChildNodeSearched = false;
				}
			}
			//条件を満たす時改めてleafを加算
			countLeafNode(node,isAllOfChildNodeSearched);
		}

		//葉ノードになるまで再帰的に処理させる
		for(Node child : node.getChildren()){
			//探索前であれば子ノードを探索
			if(!child.getIsSearched()){
				move(child,node);
			}
		}

		//"isAllOfChildNodeSearched"の条件を満たしていなければ自分を調整する
		if(!isAllOfChildNodeSearched){
			reArrangeNodeY(node);
		}
		//経過をViewに伝え、アニメーションの時間を管理する
		processReport();
		return;
	}

	/**
	 * 探索しているノードのmodel座標を求める。
	 * @param node 探索中のノード
	 * @param parent 探索中の直属の親ノード
	 * @return
	 */
	public Point putNodeModelPoint(Node node,Node parent){

		//ノードのx座標とy座標
		int x;//parentX()  + distance
		int y;//parent Y + height * leaf
		Point aPoint;

		//ルートノードの初期位置ずれ
		int rootNodeY = 30;

		//親ノードがない時、根ノードとして扱う
		if(node.getParents().size() == 0){
			x = 200;
			y = 30 + 20*leaf;
			rootNodeY = y;
		}
		//それ以外のときは親ノードの座標を参考にmodel座標を計算する.
		else{
			x = (int)parent.getPoint().getX() + parent.getTextWidth()+ 10 + distance;
			y = rootNodeY + leaf * height;
		}
		//モデル座標を設定
		aPoint = new Point(x,y);
		return aPoint;
	}

	/**
	 * アニメーションのために遅延時間を設定する
	 */
	public void tickTock(){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return;
	}

	/**
	 * 探索中のNodeが葉ノードのときleafを加算する
	 * @param node
	 */
	public void countLeafNode(Node node,boolean isAllOfChildNodeSearched){
		if(node.getChildren().size()==0){
			leaf = leaf +1;
		}else
		{
			if(isAllOfChildNodeSearched){
				leaf = leaf +1;
			}
		}
		return;
	}

	/**
	 * 子ノードを探索し終えたノードのView座標を更新する
	 * @param node 子ノードを探索し終えたノード
	 */
	public void reArrangeNodeY(Node node){
		int y_childOfAve = 0;
		if(node.getChildren().size() != 0){
			for(int i = 0; i < node.getChildren().size();i++){
				y_childOfAve += node.getChildren().get(i).getViewPoint().getY();
			}
			y_childOfAve = (int)y_childOfAve/node.getChildren().size();
		}
		else{
			y_childOfAve =  (int)node.getPoint().getY();
		}
		node.setViewPoint(new Point((int)node.getPoint().getX(), y_childOfAve));
		return;
	}

	public void processReport(){
		//変更をViewに伝える
		this.changed();

		//アニメーションのための停止時間を管理
		tickTock();
	}

	/**
	 * ツリー構造を標準出力する
	 * @param node 調査中のメソッド
	 * @param time　ノードの深さ
	 */
	public void print(Node node,Integer time){

		time = time + 1;
		String branch = "+ ";
		for(int i = 0; i < time; i++){ branch = branch + "- " ;}

		System.out.println(branch+node.getName());

		for(Node child : node.getChildren()){
			print(child,time);
		}
		return;
	}

	/**
	 * このインスタンスを文字列にして応答する。
	 * @return 自分自身を表す文字列
	 */
	public String toString()
	{
		StringBuffer aBuffer = new StringBuffer();
		Class<?> aClass = this.getClass();
		aBuffer.append(aClass.getName());
		/*
		aBuffer.append("[picture=");
		aBuffer.append(picture);
		aBuffer.append("]");
		*/
		return aBuffer.toString();
	}

	/**
	 * ノードのリストを応答する
	 * @return nodes ノードの全リスト
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	/**
	 * ノードのリストを設定する。
	 * @param nodes　ノードのリスト
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

}
