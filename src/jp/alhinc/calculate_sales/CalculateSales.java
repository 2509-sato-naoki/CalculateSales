package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LIST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "が存在しません";
	private static final String FILE_INVALID_FORMAT = "のフォーマットが不正です";
	private static final String FILE_NOT_SERIAL_NUMBER = "売上ファイル名が連番になっていません";
	private static final String SALE_AMOUNT_OVER_10_DIGIT = "合計金額が10桁を超えました";
	private static final String INVALID_FORMAT = "のフォーマットが不正です";
	private static final String INVALID_BRANCH_CODE = "の支店コードが不正です";
	private static final String INVALID_COMMODITY_CODE = "の商品コードが不正です";

	// ファイル名の定数（日本語）エラーメッセージ用
	private static final String BRANCH = "支店定義ファイル";
	private static final String COMMODITY = "商品定義ファイル";

	// ファイルごとの正規表現
	private static final String REGEX_BRANCH = "^[0-9]{3}$";
	private static final String REGEX_COMMODITY = "[A-Za-z0-9]{8}$";
	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		// ここで初めてargsが使われるので、ここでargsの確認する
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, REGEX_BRANCH, BRANCH, branchNames, branchSales)) {
			return;
		}

		// 商品定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_COMMODITY_LIST, REGEX_COMMODITY, COMMODITY, commodityNames, commoditySales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// ①まずはすべてのファイルを取得する
		File[] files = new File(args[0]).listFiles();

		// ②正規表現を用いて取得したファイルが売り上げファイルかどうか判定する
		// ③売り上げファイルに当たるものをListに格納
		List<File> rcdFile = new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && files[i].getName().matches("^[0-9]{8}.rcd$")) {
				rcdFile.add(files[i]);
			}
		}
		Collections.sort(rcdFile);
		// 売り上げファイルが連番か確認する処理
		for (int i = 0; i < rcdFile.size() - 1; i++) {
			int former = Integer.parseInt(rcdFile.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFile.get(i + 1).getName().substring(0, 8));

			if ((latter - former) != 1) {
				System.out.println(FILE_NOT_SERIAL_NUMBER);
				return;
			}
		}

		// ④Listに格納したファイルをすべて読み込む
		for (int i = 0; i < rcdFile.size(); i++) {
			BufferedReader br = null;
			try {
				// rcdFile.get(i)がファイル型の変数
				FileReader fr = new FileReader(rcdFile.get(i));
				br = new BufferedReader(fr);

				String line;
				List<String> list = new ArrayList<String>();
				while ((line = br.readLine()) != null) {
					list.add(line);
				}
				//売上ファイルのフォーマット確認（3行かどうか）をする場所はここ
				if (list.size() != 3) {
					System.out.println(rcdFile.get(i).getName() + INVALID_FORMAT);
					return;
				}
				// 売上ファイルの⽀店コードが⽀店定義ファイルに存在するか確認する処理はここ
				if (!branchNames.containsKey(list.get(0))) {
					System.out.println(rcdFile.get(i).getName() + INVALID_BRANCH_CODE);
					return;
				}
				if (!commodityNames.containsKey(list.get(1))) {
					System.out.println(rcdFile.get(i).getName() + INVALID_COMMODITY_CODE);
					return;
				}
				if (!list.get(2).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				Long fileSale = Long.parseLong(list.get(2));
				Long saleAmountBranch = branchSales.get(list.get(0)) + fileSale;
				// 該当商品の合計金額
				Long saleAmountCommodity = commoditySales.get(list.get(1)) + fileSale;
				// 合計⾦額が10桁を超えたかどうかの確認
				if (saleAmountBranch > 10000000000L || saleAmountCommodity > 10000000000L) {
					System.out.println(SALE_AMOUNT_OVER_10_DIGIT);
					return;
				}
				branchSales.put(list.get(0), saleAmountBranch);
				commoditySales.put(list.get(1), saleAmountCommodity);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
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

	//引数の命名を変える役割を考える
	private static boolean readFile(String path, String fileName, String rejex, String fileKinds, Map<String, String> names,
			Map<String, Long> sales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			// ファイルがあるかどうか確認
			if (!file.exists()) {
				System.out.println(fileKinds + FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] storeNameCode = line.split(",");

				// 商品定義ファイルの追加における変更点、正規表現、if文、エラー文の出し分け
				if (storeNameCode.length != 2 || !storeNameCode[0].matches(rejex)) {
					System.out.println(fileKinds + FILE_INVALID_FORMAT);
					return false;
				}
				names.put(storeNameCode[0], storeNameCode[1]);
				sales.put(storeNameCode[0], 0L);

			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
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
	private static boolean writeFile(String path, String fileName, Map<String, String> names,
			Map<String, Long> sales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			//	ここでまずファイルを作成する
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (String key : names.keySet()) {
				bw.write(key);
				bw.write(",");
				bw.write(names.get(key));
				bw.write(",");
				bw.write(Long.toString(sales.get(key)));
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
