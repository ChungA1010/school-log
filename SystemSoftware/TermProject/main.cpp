#include <iostream>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <map>
#include <string>
#include <regex>
#include <vector>
#include <algorithm>
using namespace std;

map<string, string> OPTAB;
map<string, int> SYMTAB;
map<string, int> LITTAB;

int LOCCTR = 0;
int STARTADDR = 0;
int LASTADDR = 0;

// ────────────────────────────────
// 유틸 함수
// ────────────────────────────────
string trim(const string &s) {
    string res = s;
    res.erase(remove(res.begin(), res.end(), '\t'), res.end());
    while (res.find("  ") != string::npos)
        res.replace(res.find("  "), 2, " ");
    if (!res.empty() && res[0] == ' ') res.erase(0, 1);
    if (!res.empty() && res.back() == ' ') res.pop_back();
    return res;
}

int getOperandSize(const string &operand) {
    if (operand.empty()) return 0;
    if (operand[0] == 'C') return operand.size() - 3;
    if (operand[0] == 'X') return (operand.size() - 3) / 2;
    return 0;
}

void loadOPTAB() {
    ifstream fin("optab.txt");
    string line;
    while (getline(fin, line)) {
        line = trim(line);
        if (line.empty() || line[0] == '.') continue; // 주석 무시

        // 모든 구분자 :, , 을 공백으로 치환
        for (char &c : line)
            if (c == ':' || c == ',' || c == ';' || c == '=') c = ' ';

        string opcode, hex;
        stringstream ss(line);
        ss >> opcode >> hex;

        if (opcode.empty() || hex.empty()) continue;

        for (auto &c : opcode) c = toupper(c);
        for (auto &c : hex) c = toupper(c);

        OPTAB[opcode] = hex;
    }
    fin.close();
}


// ────────────────────────────────
// PASS 1
// ────────────────────────────────
void pass1() {
    ifstream fin("SRCFILE.txt");
    ofstream fout("intfile.txt");
    if (!fin.is_open()) {
        cerr << "❌ input.txt 파일을 열 수 없습니다.\n";
        return;
    }

    loadOPTAB();
    string line;
    int lineNo = 0;
    SYMTAB.clear();
    LITTAB.clear();

    fout << left
         << setw(6) << "LINE"
         << setw(8) << "LOC"
         << setw(12) << "LABEL"
         << setw(12) << "OPCODE"
         << setw(20) << "OPERAND" << "\n";
    fout << string(60, '-') << "\n";

    // 첫 줄 (START)
    getline(fin, line);
    line = trim(line);
    vector<string> tokens;
    string word;
    stringstream ss1(line);
    while (ss1 >> word) tokens.push_back(word);

    string label = "-", opcode = "", operand = "";
    if (tokens.size() >= 2) {
        label = tokens[0];
        opcode = tokens[1];
        operand = (tokens.size() == 3) ? tokens[2] : "";
    }

    for (auto &c : opcode) c = toupper(c);

    if (opcode == "START") {
        STARTADDR = stoi(operand, nullptr, 16);
        LOCCTR = STARTADDR;
    }

    fout << right << setw(4) << setfill('0') << lineNo++
         << "  " << hex << uppercase << setw(6) << setfill('0') << LOCCTR
         << "  " << setfill(' ')
         << left << setw(12) << label
         << setw(12) << opcode
         << setw(20) << operand << "\n";

    // 본문
    while (getline(fin, line)) {
        line = trim(line);
        if (line.empty() || line[0] == '.') continue;

        vector<string> parts;
        string tok;
        stringstream ss(line);
        while (ss >> tok) parts.push_back(tok);

        label = "-"; opcode = ""; operand = "";
        if (parts.size() == 3) {
            label = parts[0]; opcode = parts[1]; operand = parts[2];
        } else if (parts.size() == 2) {
            opcode = parts[0]; operand = parts[1];
        } else if (parts.size() == 1) {
            opcode = parts[0];
        }

        for (auto &c : opcode) c = toupper(c);

        // 출력
        fout << right << setw(4) << setfill('0') << lineNo++
             << "  " << hex << uppercase << setw(6) << setfill('0') << LOCCTR
             << "  " << setfill(' ')
             << left << setw(12) << label
             << setw(12) << opcode
             << setw(20) << operand << "\n";

        // ────────────────────────────────
        // EQU 처리
        // ────────────────────────────────
        if (opcode == "EQU") {
            int value = 0;
            if (operand == "*") value = LOCCTR;
            else if (SYMTAB.count(operand)) value = SYMTAB[operand];
            else if (regex_match(operand, regex("[0-9A-Fa-f]+")))
                value = stoi(operand, nullptr, 16);
            else {
                smatch m;
                if (regex_match(operand, m, regex("([A-Za-z0-9]+)\\s*([-+])\\s*([A-Za-z0-9]+)"))) {
                    string s1 = m[1], s2 = m[3];
                    int v1 = SYMTAB.count(s1) ? SYMTAB[s1] : stoi(s1, nullptr, 16);
                    int v2 = SYMTAB.count(s2) ? SYMTAB[s2] : stoi(s2, nullptr, 16);
                    value = (m[2] == "+") ? v1 + v2 : v1 - v2;
                }
            }
            SYMTAB[label] = value;
            continue;
        }

        // ────────────────────────────────
        // ORG 처리
        // ────────────────────────────────
        if (opcode == "ORG") {
            if (SYMTAB.count(operand))
                LOCCTR = SYMTAB[operand];
            else if (!operand.empty())
                LOCCTR = stoi(operand, nullptr, 16);
            continue;
        }

        // ────────────────────────────────
        // 리터럴 수집
        // ────────────────────────────────
        if (!operand.empty() && operand[0] == '=') {
            if (!LITTAB.count(operand))
                LITTAB[operand] = -1; // 주소 미배정 상태
        }

        // ────────────────────────────────
        // LTORG 처리
        // ────────────────────────────────
        if (opcode == "LTORG") {
            for (auto &lit : LITTAB)
                if (lit.second == -1) {
                    lit.second = LOCCTR;
                    LOCCTR += getOperandSize(lit.first.substr(1)); // =C'EOF' → C'EOF'
                }
            continue;
        }

        // ────────────────────────────────
        // SYMTAB 저장
        // ────────────────────────────────
        if (label != "-") SYMTAB[label] = LOCCTR;

        // ────────────────────────────────
        // LOCCTR 증가
        // ────────────────────────────────
        if (OPTAB.count(opcode)) LOCCTR += 3;
        else if (opcode == "WORD") LOCCTR += 3;
        else if (opcode == "RESW") LOCCTR += 3 * stoi(operand);
        else if (opcode == "RESB") LOCCTR += stoi(operand);
        else if (opcode == "BYTE") LOCCTR += getOperandSize(operand);
        else if (opcode == "END") {
            for (auto &lit : LITTAB)
                if (lit.second == -1) {
                    lit.second = LOCCTR;
                    LOCCTR += getOperandSize(lit.first.substr(1));
                }
            LASTADDR = LOCCTR;
            break;
        }
    }

    fin.close();
    fout.close();

        fin.close();
    fout.close();

    // ────────────────────────────────
    // SYMTAB 출력
    // ────────────────────────────────
    ofstream fsym("SYMTAB.txt");
    fsym << left << setw(12) << "SYMBOL" << setw(10) << "ADDRESS" << "\n";
    fsym << string(22, '-') << "\n";

    vector<pair<string, int> > sortedSym(SYMTAB.begin(), SYMTAB.end());
    sort(sortedSym.begin(), sortedSym.end());

    for (auto &p : sortedSym) {
        fsym << left << setw(12) << p.first
            << right << setw(10) << setfill(' ') << uppercase << hex << p.second << "\n";
    }
    fsym.close();

    // ────────────────────────────────
    // LITTAB 출력
    // ────────────────────────────────
    ofstream flit("LITTAB.txt");
    flit << left << setw(12) << "LITERAL" << setw(10) << "ADDRESS" << "\n";
    flit << string(22, '-') << "\n";

    vector<pair<string, int> > sortedLit(LITTAB.begin(), LITTAB.end());
    sort(sortedLit.begin(), sortedLit.end());

    for (auto &p : sortedLit) {
        flit << left << setw(12) << p.first
            << right << setw(10) << setfill(' ') << uppercase << hex << p.second << "\n";
    }
    flit.close();


    cout << "[PASS1 완료] intfile.txt 생성됨 ✅\n";
}

// ────────────────────────────────
// OBJ CODE 생성
// ────────────────────────────────
string makeObjCode(string opcode, string operand) {
    string code = OPTAB[opcode];
    if (operand.empty()) {
        code += "0000";
        return code;
    }

    // 리터럴 참조
    if (operand[0] == '=') {
        if (LITTAB.count(operand)) {
            stringstream ss;
            ss << setw(4) << setfill('0') << hex << LITTAB[operand];
            code += ss.str();
        } else code += "0000";
    }
    // 일반 심볼
    else if (SYMTAB.count(operand)) {
        stringstream ss;
        ss << setw(4) << setfill('0') << hex << SYMTAB[operand];
        code += ss.str();
    } else {
        code += "0000";
    }
    return code;
}

// ────────────────────────────────
// PASS 2
// ────────────────────────────────
void pass2() {
    ifstream fin("intfile.txt");
    ofstream fout("objfile.txt");
    if (!fin.is_open()) {
        cerr << "❌ intfile.txt 파일을 열 수 없습니다.\n";
        return;
    }

    string line, loc, label, opcode, operand;
    string progName;
    int startAddr = 0, firstExec = 0;
    const int MAX_TEXT_LEN = 60;

    getline(fin, line); // 헤더 skip
    getline(fin, line);

    getline(fin, line);
    stringstream ss(line);
    int lineNo;
    ss >> lineNo >> loc >> label >> opcode >> operand;

    if (opcode == "START") {
        progName = label;
        startAddr = stoi(operand, nullptr, 16);
        firstExec = startAddr;
    }

    stringstream textRecords;
    string textData = "";
    int textStart = 0, textLen = 0;

    while (getline(fin, line)) {
        if (line.empty()) continue;
        stringstream ss(line);
        ss >> lineNo >> loc >> label >> opcode >> operand;
        for (auto &c : opcode) c = toupper(c);

        if (opcode == "END") break;
        if (opcode == "RESW" || opcode == "RESB" || opcode == "LTORG" || opcode == "ORG" || opcode == "EQU")
            continue;

        string obj = (OPTAB.count(opcode)) ? makeObjCode(opcode, operand) : "";

        if (!obj.empty()) {
            if (textData.empty())
                textStart = stoi(loc, nullptr, 16);

            if ((textData.size() + obj.size()) > MAX_TEXT_LEN) {
                textRecords << "T" << setw(6) << setfill('0') << right << hex << textStart
                            << setw(2) << textLen << textData << "\n";
                textData = "";
                textLen = 0;
                textStart = stoi(loc, nullptr, 16);
            }

            textData += obj;
            textLen = textData.size() / 2;
        }
    }

    if (!textData.empty()) {
        textRecords << "T" << setw(6) << setfill('0') << right << uppercase << hex << textStart
                    << setw(2) << textLen << textData << "\n";
    }

    int progLen = LASTADDR - STARTADDR;
    if (progLen < 0) progLen = 0;

    fout << "H" << setw(6) << left << progName.substr(0,6)
         << setw(6) << setfill('0') << right << uppercase << hex << STARTADDR
         << setw(6) << setfill('0') << right << uppercase << hex << progLen << "\n";
    fout << textRecords.str();
    fout << "E" << setw(6) << setfill('0') << uppercase << hex << firstExec << "\n";

    fout.close();
    cout << "[PASS2 완료] objfile.txt 생성됨 ✅\n";
}

// ────────────────────────────────
// MAIN
// ────────────────────────────────
int main() {
    pass1();
    pass2();
    return 0;
}
