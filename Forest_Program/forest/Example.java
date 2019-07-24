package forest;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 樹状整列の例題クラス：使い方の典型を示すのが目的のプログラムです。<br>
 * Makefileを用いた実行方法は以下の通りです。<br>
 * $ make tree  # 木を整列描画<br>
 * $ make forest  # 森を整列描画<br>
 * $ make semilattice  # 亜格子状の森を整列描画<br>
 */
public class Example extends Object
{
	/**
	 * 第1引数で樹状整列データファイルを受け取って樹状整列を実行します。<br>
	 * $ java -Dfile.encoding=UTF-8 -Xmx512m -Xss1024k -jar forest.jar resource/data/tree.txt<br>
	 * $ java -Dfile.encoding=UTF-8 -Xmx512m -Xss1024k -jar forest.jar resource/data/forest.txt<br>
	 * $ java -Dfile.encoding=UTF-8 -Xmx512m -Xss1024k -jar forest.jar resource/data/semilattice.txt<br>
	 * @param arguments 樹状整列データファイルを第1引数とする引数文字列群
	 * @throws FileNotFoundException 指定されたパス名で示されるファイルが開けなかったことを通知
	 */
	public static void main(String[] arguments) throws FileNotFoundException
	{
		JFileChooser chooser = new JFileChooser();

		String currentDirectory = System.getProperty("user.dir");
		String dataDirectory = currentDirectory +  File.separator + "resource" + File.separator + "data" + File.separator + "tree.txt";

		//デフォルトの選択ファイルを指定
		chooser.setSelectedFile(new File(dataDirectory));

		FileFilter filter = new FileNameExtensionFilter("textファイル", "txt");
		chooser.addChoosableFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);

		chooser.setDialogTitle("ファイルを読み込む");

		//ダイアログを表示
		int selected = chooser.showOpenDialog(null);
		File aFile;
		if(selected == JFileChooser.APPROVE_OPTION){
			aFile = chooser.getSelectedFile();

			// MVCを作成する。
			ForestModel aModel = new ForestModel(aFile);
			ForestView aView = new ForestView(aModel);

			// ウィンドウを生成して開く。
			JFrame aWindow = new JFrame(aFile.getName());
			aWindow.getContentPane().add(aView);
			aWindow.setMinimumSize(new Dimension(400, 300));
			aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			aWindow.setSize(800, 600);
			aWindow.setLocationRelativeTo(null);
			aWindow.setVisible(true);

			// 樹状整列のアニメーションを行う。
			aModel.animate();
        }

		return;
	}
}
