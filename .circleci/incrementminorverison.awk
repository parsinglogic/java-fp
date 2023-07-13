$1 == "version:" {
	split($2,array,"[.-]")
	$2 = array[1] "." array[2]+1 "."  array[3] "-" array[4]
}
{
	print $0
}