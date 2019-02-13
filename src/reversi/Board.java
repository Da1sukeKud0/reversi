package reversi;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Board {
	private static final int BoardSize = 8, empty = 0, black = 1, white = 2, surrender = 111, pass = 222;
	private static int numOfempty = BoardSize ^ 2 - 4;
	private static int owner = black; // put()終了時にpass()で白黒反転
	private static int enemy = white; // put()終了時にpass()で白黒反転
	private static int[][] boardStatus = new int[BoardSize][BoardSize];

	public Board() {
		// boardStatusは8*8の0で初期化された配列
		boardStatus[3][3] = white;
		boardStatus[3][4] = black;
		boardStatus[4][3] = black;
		boardStatus[4][4] = white;
	}

	// 石を置くとき
	public void put() {
		disp();
		System.out.println("");
		System.out.println(toZenkakuMasu(owner) + "のターン");
		int x, y;
		do {
			System.out.print("ヨコ：");
			x = inputAndRangeCheck();
			System.out.print("タテ：");
			y = inputAndRangeCheck();
		} while (!validationCheck(x, y));
	}

	@SuppressWarnings("resource")
	private static int inputAndRangeCheck(int min, int max) {
		int num;
		while (true) {
			try {
				num = new Scanner(System.in).nextInt();
			} catch (NumberFormatException | InputMismatchException e) {
				System.out.print("半角数字で入力してください。再入力：");
				continue;
			}
			if (min <= num && num <= max)
				break;
			else
				System.out.print("値が範囲外です。再入力：");
		}
		return num;
	}

	private static int inputAndRangeCheck() {
		return inputAndRangeCheck(0, BoardSize - 1);
	}

	// 石が返せるかの判定及び実行
	private static boolean validationCheck(int x, int y) {
		// 指定された位置が空か
		if (boardStatus[x][y] != 0) {
			System.out.println("既に石があります。");
			return false;
		}
		// 周囲に石があるか
		boolean flag = false;
		for (int i = x - 1; i <= x + 1; i++) {
			if (i < 0 || BoardSize <= i)
				continue;
			for (int j = y - 1; j <= y + 1; j++) {
				if (j < 0 || BoardSize <= j)
					continue;
				if (!outOfRangeCheck(i, j))
					continue;
				if (boardStatus[i][j] != enemy)
					continue; // 敵色であるか
				int vx = i - x;
				int vy = j - y;
				// System.out.println("check now: " + x + "," + y + " vec: " + vx + "," + vy);
				flag = (vectorSearch(x, y, vx, vy) == true) ? true : flag;
			}
		}
		// 石を返せたかの判定後の処理
		if (flag) {
			boardStatus[x][y] = owner;
			// ターン終了処理
			pass();
			numOfempty -= 1;
			if (numOfempty == 0)
				toryo();
			return true;
		} else {
			System.out.println("石が返せません。諦める？");
			while (true) {
				System.out.print("(諦めない:* 諦める:111 パスする:222)：");
				int stdin = inputAndRangeCheck(0, 2147483647);
				if (stdin == surrender) {
					System.out.println("投了です。");
					toryo();
				} else if (stdin == pass) {
					pass();
					return true;
				} else
					break;
			}
			return false;
		}
	}

	// x,yを基準としvx,vyベクトル方向に探索して石が返せるか確認
	private static boolean vectorSearch(int x, int y, int vx, int vy) {
		ArrayList<Integer[]> reversibleList = new ArrayList<Integer[]>();
		int ax = vx;
		int ay = vy;
		while (true) {
			int i = x + vx;
			int j = y + vy;
			if (!outOfRangeCheck(i, j))
				break;
			if (boardStatus[i][j] == enemy) {
				Integer[] tmp = { i, j };
				reversibleList.add(tmp);
				vx += ax;
				vy += ay;
				continue;
			} else if (boardStatus[i][j] == owner) {
				reverse(reversibleList); // 石を返す
				return true;
			} else
				break;
		}
		return false;
	}

	// 指定された座標がボード範囲内ならTrue
	private static boolean outOfRangeCheck(int i, int j) {
		return 0 <= i && 0 <= j && i < BoardSize && j < BoardSize;
	}

	// 返せる石を返す
	private static void reverse(ArrayList<Integer[]> reversibleList) {
		for (Integer[] pos : reversibleList) {
			boardStatus[pos[0]][pos[1]] = owner;
		}
	}

	// 相手に打席を譲る
	private static void pass() {
		owner = (owner == black) ? white : black;
		enemy = (enemy == black) ? white : black;
	}

	// 投了処理
	private static void toryo() {
		int numOfBlack = 0, numOfWhite = 0;
		for (int i = 0; i < BoardSize; i++) {
			for (int j = 0; j < BoardSize; j++) {
				if (boardStatus[i][j] == black)
					numOfBlack++;
				if (boardStatus[i][j] == white)
					numOfWhite++;
			}
		}
		System.out.println(toZenkakuMasu(black) + "の数：" + numOfBlack);
		System.out.println(toZenkakuMasu(white) + "の数：" + numOfWhite);
		if (numOfWhite < numOfBlack)
			System.out.println("勝者は" + toZenkakuMasu(black) + "です。");
		else if (numOfBlack < numOfWhite)
			System.out.println("勝者は" + toZenkakuMasu(white) + "です。");
		else
			System.out.println("引き分けです。");
		System.exit(0);
	}

	// コンソール出力
	private static void disp() {
		// ヘッダー
		setBlackColor();
		System.out.print("  ");
		for (int i = 0; i < BoardSize; i++) {
			System.out.print(toZenkakuNum(i));
		}
		// System.out.println("");
		// System.out.print("ｙ");
		// System.out.print(" ーーーーーーーー ");
		System.out.print("  ");
		setNoneColor();
		System.out.println("");
		for (int j = 0; j < BoardSize; j++) {
			setBlackColor();
			System.out.print(toZenkakuNum(j));
			// System.out.print("｜");
			setNoneColor();
			setBoardColor();
			for (int i = 0; i < BoardSize; i++) {
				System.out.print(toZenkakuMasu(boardStatus[i][j]));
			}
			setNoneColor();
			setBlackColor();
			// System.out.print("｜");
			System.out.print(toZenkakuNum(j));
			setNoneColor();
			System.out.println("");
		}
		// フッター
		// System.out.print(" ");
		setBlackColor();
		// System.out.print(" ーーーーーーーー ");
		// System.out.println(" ");
		System.out.print("  ");
		for (int i = 0; i < BoardSize; i++) {
			System.out.print(toZenkakuNum(i));
		}
		System.out.print("  ");
		setNoneColor();
		System.out.println("");
	}

	// マスの全角ビジュアル表示
	private static String toZenkakuMasu(int boardStatus) {
		String str = new String();
		switch (boardStatus) {
		case white:
			str = "⚪";
			break;
		case black:
			str = "⚫";
			break;
		case empty:
			str = "・";
			break;
		}
		return str;
	}

	// 半角数字を全角数字に変換
	public static String toZenkakuNum(int num) {
		String s = Integer.toString(num);
		StringBuffer sb = new StringBuffer(s);
		if (num <= 9) {
			for (int i = 0; i < sb.length(); i++) {
				int c = (int) sb.charAt(i);
				if (c >= 0x30 && c <= 0x39) {
					sb.setCharAt(i, (char) (c + 0xFEE0));
				}
			}
		}
		return sb.toString();
	}

	// ボードの背景色を設定
	private static void setBoardColor() {
		System.out.print("\u001b[00;42m");
	}

	// ボード縁の背景色を設定
	private static void setBlackColor() {
		System.out.print("\u001b[00;40m");
	}

	// ボードの背景色を無効化
	private static void setNoneColor() {
		System.out.print("\u001b[00m");
	}
}
