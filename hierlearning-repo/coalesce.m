function average = coalesce(filename_str, total_runs)
% Coalescer
% Command line: octave --eval "coalesce <filename>"

run = 1;
curve = [];
failed_runs = 0;
for run = 1:total_runs
	filename = sprintf('%s_%u.out', filename_str, run);
	if (exist(filename, 'file'))
		temp = load(filename);
		curve(:,end+1) = temp(:,end);
	else
		++failed_runs;
	end
end

if size(curve) > 0
	filename = sprintf('%s_coalesced.out', filename_str);
	dlmwrite(filename, curve, '\t');

	filename = sprintf('%s.out', filename_str);
	dlmwrite(filename, mean(curve, 2), '\t');
end
printf("Success = %f%%\n", (total_runs - failed_runs)*100/total_runs);
end
