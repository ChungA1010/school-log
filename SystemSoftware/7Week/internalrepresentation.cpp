#include <iostream>
#include <fstream>
#include <string>
#include <iomanip>
using namespace std;

int hexToDecimal(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    else if (c >= 'A' && c <= 'F') return c - 55; // 'A'=65 → 10
    else if (c >= 'a' && c <= 'f') return c - 87;
    else return -1;
}

int main() {
    ifstream fin("objfile.txt");
    string line;

    while (getline(fin, line)) {
        if (line.empty() || line[0] != 'T') continue;

        string tag = line.substr(0, 1);
        string addr = line.substr(1, 6);
        string length = line.substr(7, 2);
        string codes = line.substr(9);

        cout << "Tag=" << tag 
             << ", Addr=" << addr 
             << ", Length=" << length 
             << ", Codes=" << codes << endl;

        cout << "InCODES(decimal): ";
        for (char c : codes) {
            int val = hexToDecimal(c);
            if (val >= 0)
                cout << val << " ";
        }
        cout << endl;

        cout << "InCODES(binary): ";
        for (char c : codes) {
            int val = hexToDecimal(c);
            if (val >= 0) {
                for (int i = 3; i >= 0; i--)
                    cout << ((val >> i) & 1);
                cout << " ";
            }
        }
        cout << "\n----------------------------------\n";
    }

    fin.close();
    return 0;
}
