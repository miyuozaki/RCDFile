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


	public static void main(String[] args) throws IOException {

//支店定義ファイル読み込み1

		HashMap<String,String> branchlst = new HashMap<String,String>();
		HashMap<String,Long>branchsale = new HashMap<String,Long>();
		BufferedReader br;
		FileReader fr;
		br =null;
		fr = null;

//コマンドライン引数が渡されていない場合
		if(args[0] == null){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

//コマンドライン引数が二つ以上あった場合

		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		try{

			File file  = new File(args[0], "branch.lst");

			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String fileLine;

			if (!file.exists()) {
				System.out.println("支店定義ファイルが存在しません");
				return;
			}else{

				while((fileLine = br.readLine())!= null){

					String items[] = fileLine.split(",",-1);

					if(!items[0].matches("^[0-9]*$")){
						System.out.println("支店定義ファイルのフォーマットが不正です");
						return;
					}
					if(!items[0].matches("^\\d{3}$") || (items.length != 2)){
						System.out.println("支店定義ファイルのフォーマットが不正です");
						return;
					}

					branchlst.put(items[0],items[1]);
					branchsale.put(items[0],0L);
				}
			}
		}catch(FileNotFoundException e){
			System.out.println("支店定義ファイルが存在しません");
			return;
		}finally{
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

//商品定義ファイル読み込み

		HashMap<String,String> commoditylst = new HashMap<String,String>();
		HashMap<String,Long>commoditysale = new HashMap<String,Long>();

		try{

			File file  = new File(args[0], "commodity.lst");
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String fileLine;

			if (!file.exists()) {
				System.out.println("商品定義ファイルが存在しません");
				return;
			}else{

				while((fileLine = br.readLine())!= null){

					String items[] = fileLine.split(",",-1);

					if(!items[0].matches("^[0-9A-Z]+$")){
						System.out.println("商品定義ファイルのフォーマットが不正です");
						return;
					}

					if(!items[0].matches("\\w{8}$") || (items.length != 2)){
						System.out.println("商品定義ファイルのフォーマットが不正です");
						return;
					}
					commoditylst.put(items[0],items[1]);
					commoditysale.put(items[0], 0L);
				}
			}

		}catch(FileNotFoundException e){
			System.out.println("商品定義ファイルが存在しません");
		}finally{

			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

//00000001.rcd 連番チェック

		ArrayList<File> rcdFiles = new ArrayList<File>();
		ArrayList<String> rcdNumber = new ArrayList<String>();
		File dir = new File(args[0]);
		File[] rcdLst = dir.listFiles();

		FileWriter fw;
		BufferedWriter bw;
		bw = null;

		try{

			//ディレクトリの中からRCDファイルの取得
			for (int i = 0; i < rcdLst.length; i++) {

				if (rcdLst[i].isFile() && rcdLst[i].getName().matches("^\\d{8}\\.rcd$")) {
					rcdFiles.add(rcdLst[i]);
					String[] array2 = rcdLst[i].getName().split("\\.");
					rcdNumber.add(array2[0]);
				}
			}

			Collections.sort(rcdNumber);
			//連番チェック
			for (int i = 0; i < rcdNumber.size() - 1; i++) {
				int j = Integer.parseInt(rcdNumber.get(i));
				int k = Integer.parseInt(rcdNumber.get(i + 1));

				if (k - j != 1) {
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}

			}

//集計 & エラーチェック

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
					System.out.println(rcdArray.get(0) +  "のフォーマットが不正です");
					return;
				}


				if(rcdArray.get(2).matches(" [^0-9]")) {
					System.out.println(rcdArray.get(2) + "のフォーマットが不正です");
					return;
				}

				//売り上げファイルの支店コードが存在しない
				if(!branchsale.containsKey(rcdArray.get(0))){
					System.out.println((rcdArray.get(0)) + ".rcdの支店コードが不正です");
					return;
				}
				//売り上げファイルの商品コードが存在しない
				if(!commoditysale.containsKey(rcdArray.get(1))){
					System.out.println((rcdArray.get(1)) + ".rcdの商品コードが不正です");
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
		}finally{
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

//ファイル書き込み

		try{

			List<Entry<String,Long>> entriesBra =
					new ArrayList<Entry<String,Long>>(branchsale.entrySet());
			Collections.sort(entriesBra, new Comparator<Entry<String,Long>>() {
			@Override
			public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}});

			File bFile  = new File(args[0],"branch.out");

			fw = new FileWriter(bFile);
			bw = new BufferedWriter(fw);

			for (Entry<String,Long> s : entriesBra) {
				bw.write(s.getKey() + "," + branchlst.get(s.getKey()) + "," + s.getValue() + "\n");
			}

		}catch(FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		try{

			List<Entry<String,Long>> entriesCom =
					new ArrayList<Entry<String,Long>>(commoditysale.entrySet());
			Collections.sort(entriesCom, new Comparator<Entry<String,Long>>() {
			@Override
			public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}});

			File cFile  = new File(args[0],"commodity.out");

			fw = new FileWriter(cFile);
			bw = new BufferedWriter(fw);

			for (Entry<String,Long> s : entriesCom) {
				bw.write(s.getKey() + ","  + commoditylst.get(s.getKey()) + ","  + s.getValue() + "\n");

			}

		}catch(FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
	}
}





