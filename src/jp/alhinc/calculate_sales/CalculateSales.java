package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// ①まずはすべてのファイルを取得する
		File[] files = new File(args[0]).listFiles();

		// ②正規表現を用いて取得したファイルが売り上げファイルかどうか判定する
		// ③売り上げファイルに当たるものをListに格納
		List<File> rcdFile = new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().matches("[0-9]{5}.+rcd$")) {
				rcdFile.add(files[i]);
			}
		}
		// ④Listに格納したファイルをすべて読み込む
		BufferedReader br = null;
		try {
			for (int i = 0; i < rcdFile.size(); i++) {
				// rcdFile.get(i)がファイル型の変数
				FileReader fr = new FileReader(rcdFile.get(i));
				br = new BufferedReader(fr);

				String line;
				List<String> list = new ArrayList<String>();
				while((line = br.readLine()) != null) {
					list.add(line);
				}
				Long fileSale = Long.parseLong(list.get(1));
				Long saleAmount = branchSales.get(list.get(0)) + fileSale;
				branchSales.put(list.get(0), saleAmount);
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String [] storeNameCode = line.split(",");
				branchNames.put(storeNameCode[0],storeNameCode[1]);
				branchSales.put(storeNameCode[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			//	ここでまずファイルを作成する
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (String key : branchNames.keySet()) {
				bw.write(key);
				bw.write(",");
				bw.write(branchNames.get(key));
				bw.write(",");
				bw.write(Long.toString(branchSales.get(key)));
				bw.newLine();
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
