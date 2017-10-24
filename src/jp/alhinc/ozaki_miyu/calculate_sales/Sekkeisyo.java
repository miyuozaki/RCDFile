package jp.alhinc.ozaki_miyu.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Sekkeisyo {

	 //出力メソッド
	public static boolean fileOut(String dirPath,String fileName,HashMap<String,String>outName,HashMap<String,Long>outSale){

		FileWriter fw = null;
		BufferedWriter bw = null;

		try{

			List<Entry<String,Long>> outSale1 =
					new ArrayList<Entry<String,Long>>(outSale.entrySet());
			Collections.sort(outSale1, new Comparator<Entry<String,Long>>() {
			@Override
			public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}});

			File outFile  = new File(dirPath,fileName);

			fw = new FileWriter(outFile);
			bw = new BufferedWriter(fw);

			for (Entry<String,Long> s : outSale1) {
				bw.write(s.getKey() + "," + outName.get(s.getKey()) + "," + s.getValue() + "\n");
			}

		}catch(FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}

		}

		return true;
	}

	//入力メソッド
	public static boolean fileIn(String dirPath,String fileName,String format,
			String eM,HashMap<String,String>inName,HashMap<String,Long>inSale){

		File file = new File(dirPath,fileName);
		FileReader fr = null;
		BufferedReader br = null;

		if (!file.exists()) {
			System.out.println("支店定義ファイルが存在しません");
			return false;
		}

		try{

			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String fileLine;

			while((fileLine = br.readLine())!= null){

				String items[] = fileLine.split(",",-1);

				if(!items[0].matches(format) || (items.length != 2)){
					System.out.println(eM + "定義ファイルのフォーマットが不正です");
					return false;
				}

				inName.put(items[0],items[1]);
				inSale.put(items[0],0L);
			}

		}catch(FileNotFoundException e){
			System.out.println("支店定義ファイルが存在しません");
			return false;
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}




	//メインメソッド
	public static void main(String[] args){

		//支店定義ファイル読み込み
		HashMap<String,String> branchlst = new HashMap<String,String>();
		HashMap<String,Long>branchsale = new HashMap<String,Long>();

		//商品定義ファイル読み込み
		HashMap<String,String> commoditylst = new HashMap<String,String>();
		HashMap<String,Long>commoditysale = new HashMap<String,Long>();

		String dirPath = args[0];

		//コマンドライン引数が二つ以上あった場合
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//コマンドライン引数が渡されていない場合
		if(args[0] == null){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		if (!fileIn(dirPath,"branch.lst","^\\d{3}$","支店",branchlst, branchsale)) {
			return;
		}

		if (!fileIn(dirPath,"commodity.lst","^[0-9a-zA-Z]{8}","商品",commoditylst, commoditysale)) {
			return;
		}





		ArrayList<File> rcdFiles = new ArrayList<File>();
		ArrayList<String> rcdNumber = new ArrayList<String>();
		File dir = new File(args[0]);
		File[] rcdLst = dir.listFiles();

		FileReader fr = null;
		BufferedReader br = null;

		//ディレクトリの中からRCDファイルの取得
		for (int i = 0; i < rcdLst.length; i++) {

			if (rcdLst[i].isFile() && rcdLst[i].getName().matches("^\\d{8}\\.rcd$")) {
				rcdFiles.add(rcdLst[i]);
				String[] array2 = rcdLst[i].getName().split("\\.");
				rcdNumber.add(array2[0]);
			}

			Collections.sort(rcdNumber);

			//連番チェック
			for (int h = 0; h < rcdNumber.size() - 1; h++) {
				int j = Integer.parseInt(rcdNumber.get(h));
				int k = Integer.parseInt(rcdNumber.get(h + 1));

				if (k - j != 1) {
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}

			}
		}

			//集計 & エラーチェック

		try{


			for (int i = 0; i < rcdFiles.size(); i++){

				fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				ArrayList<String> rcdArray = new ArrayList<String>();
				String amount;


				while((amount = br.readLine()) != null){
					rcdArray.add(amount);
				}
				//行数チェック
				if(rcdArray.size() != 3){
					System.out.println(rcdLst[i].getName() +  "のフォーマットが不正です");
					return;
				}

				//修正点！！！
				//金額のほうも数字以外だとエラーを出す
				if(!rcdArray.get(2).matches("^[0-9]+$")) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				//売り上げファイルの支店コードが存在しない
				if(!branchsale.containsKey(rcdArray.get(0))){
					System.out.println(rcdLst[i].getName() + "の支店コードが不正です");
					return;
				}
				//売り上げファイルの商品コードが存在しない
				if(!commoditysale.containsKey(rcdArray.get(1))){
					System.out.println(rcdLst[i].getName() + "の商品コードが不正です");
					return;
				}

				//足し算
				Long l = Long.parseLong(rcdArray.get(2));
				Long bAns = l + branchsale.get(rcdArray.get(0));
				branchsale.put(rcdArray.get(0),bAns);


				Long l2 = Long.parseLong(rcdArray.get(2));
				Long cAns = l2 + commoditysale.get(rcdArray.get(1));
				commoditysale.put(rcdArray.get(1),cAns);

				//10桁以上エラー
				if(10 < String.valueOf(bAns).length()){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				if(10 < String.valueOf(cAns).length()){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}



		}catch(FileNotFoundException e){
			System.out.println("ファイルが存在しません");
			return;
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try {
				if (br != null) {
					fr.close();
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}


		if (!fileOut(dirPath, "branch.out", branchlst, branchsale)) {
			return;
		}

		if (!fileOut(dirPath, "commodity.out", commoditylst, commoditysale)) {
			return;
		}
	}
}






